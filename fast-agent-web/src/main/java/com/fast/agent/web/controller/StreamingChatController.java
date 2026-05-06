package com.fast.agent.web.controller;

import com.fast.agent.core.flow.StepCompletionDetector;
import com.fast.agent.core.intent.IntentParser;
import com.fast.agent.core.session.SessionStepRedisService;
import com.fast.agent.core.skills.SkillsPromptLoader;
import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.model.entity.Message;
import com.fast.agent.model.entity.Session;
import com.fast.agent.model.enums.IntentType;
import com.fast.agent.service.ChatService;
import com.fast.agent.service.ConversationContextService;
import com.fast.agent.service.MessageService;
import com.fast.agent.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流式对话控制器
 * 重构：抽离 SseEmitter 创建逻辑，消除IDE黄色警告，修复时序提前关闭问题
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class StreamingChatController {

    private final ChatService chatService;
    private final SessionService sessionService;
    private final MessageService messageService;
    private final ConversationContextService contextService;
    private final IntentParser intentParser;
    private final SkillsPromptLoader skillsPromptLoader;
    private final SessionStepRedisService sessionStepRedisService;
    private final StepCompletionDetector stepCompletionDetector;

    /**
     * 构建完整消息：SystemMessage(技能Prompt) → Redis历史对话 → UserMessage(当前问题)
     * @param skillPrompt 技能Prompt
     * @param context 历史对话上下文
     * @param userQuestion 用户当前问题
     * @return 完整消息
     */
    private String buildFullMessage(String skillPrompt, String context, String userQuestion) {
        StringBuilder fullMessage = new StringBuilder();
        
        // 1. SystemMessage(技能Prompt)
        fullMessage.append("系统指令：").append(skillPrompt).append("\n\n");
        
        // 2. Redis历史对话（如果有）
        if (context != null && !context.trim().isEmpty()) {
            fullMessage.append("历史对话：").append(context).append("\n\n");
        }
        
        // 3. UserMessage(当前问题)
        fullMessage.append("用户问题：").append(userQuestion);
        
        return fullMessage.toString();
    }

    /**
     * 流式对话接口
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request) {
        // 直接调用抽离的方法创建并配置SseEmitter
        SseEmitter emitter = createConfiguredSseEmitter();
        AtomicBoolean completed = new AtomicBoolean(false);

        // 异步执行大模型流式回调，不阻塞Tomcat主线程
        new Thread(() -> {
            try {
                // 校验 sessionId 是否存在
                String sessionId = request.getSessionId();
                if (sessionId == null || sessionId.trim().isEmpty()) {
                    emitter.send(SseEmitter.event().data("sessionId 不能为空"));
                    emitter.complete();
                    completed.set(true);
                    return;
                }
                
                Session session = sessionService.getSessionById(Long.parseLong(sessionId));
                if (session == null) {
                    emitter.send(SseEmitter.event().data("会话不存在，sessionId: " + sessionId));
                    emitter.complete();
                    completed.set(true);
                    return;
                }

                log.info("流式对话开始，sessionId: {}, message: {}", sessionId, request.getMessage());
                
                // 步骤1：调用意图解析，得到当前 IntentType
                IntentType intentType = intentParser.parseIntent(request.getMessage());
                log.info("识别到用户意图：{}", intentType);
                log.info("用户问题命中技能：{} (编码：{})", intentType, intentType.getCode());
                
                // 步骤2：从内存工具类读取对应 skills 下的技能SystemPrompt
                String skillPrompt = skillsPromptLoader.getSkillPrompt(intentType);
                log.info("加载技能Prompt成功，意图：{}，Prompt长度：{}", intentType, skillPrompt.length());
                
                // 保存用户消息到数据库
                try {
                    Message userMessage = new Message();
                    userMessage.setSessionId(Long.parseLong(sessionId));
                    userMessage.setRole("user");
                    userMessage.setContent(request.getMessage());
                    userMessage.setCreateTime(java.time.LocalDateTime.now());
                    
                    Message savedUserMessage = messageService.createMessage(userMessage);
                    log.info("用户消息已保存到数据库，messageId: {}", savedUserMessage.getId());
                } catch (Exception e) {
                    log.error("保存用户消息失败", e);
                    // 即使保存失败也继续处理对话
                }
                
                // 保存用户消息到 Redis 上下文
                contextService.saveMessage(sessionId, "user", request.getMessage());
                
                // 步骤3：获取最近5轮对话上下文
                String context = contextService.formatContext(sessionId, 5);
                
                // 步骤4：拼接顺序固定：SystemMessage(技能Prompt) → Redis历史对话 → UserMessage(当前问题)
                String fullMessage = buildFullMessage(skillPrompt, context, request.getMessage());
                
                // 创建包含完整上下文的请求
                ChatRequest contextRequest = new ChatRequest();
                contextRequest.setSessionId(sessionId);
                contextRequest.setMessage(fullMessage);
                
                // 打印完整消息内容
                log.info("=== 流式对话 - 发送给大模型的完整消息 ===");
                log.info("sessionId: {}", sessionId);
                log.info("原始消息: {}", request.getMessage());
                log.info("上下文长度: {} 字符", context.length());
                log.info("完整消息长度: {} 字符", fullMessage.length());
                log.info("完整消息内容: {}", fullMessage);
                log.info("============================================");
                
                // 发送连接成功标识
                emitter.send(SseEmitter.event().data("连接成功"));
                log.info("发送连接成功消息，上下文长度: {}", context.length());

                // 用于收集完整的AI回复内容
                StringBuilder fullResponse = new StringBuilder();

                // 流式回调：逐块推送模型输出
                chatService.streamChat(contextRequest, chunk -> {
                    if (!completed.get()) {
                        try {
                            emitter.send(SseEmitter.event().data(chunk));
                            fullResponse.append(chunk);
                            log.debug("SSE 推送分片: {}", chunk);
                        } catch (Exception e) {
                            log.error("SSE 推送分片失败", e);
                        }
                    }
                });

                // 保存AI回复到数据库和 Redis 上下文
                try {
                    if (fullResponse.length() > 0) {
                        String aiResponse = fullResponse.toString();
                        
                        // 保存到数据库
                        Message aiMessage = new Message();
                        aiMessage.setSessionId(Long.parseLong(sessionId));
                        aiMessage.setRole("assistant");
                        aiMessage.setContent(aiResponse);
                        aiMessage.setCreateTime(java.time.LocalDateTime.now());
                        
                        Message savedAiMessage = messageService.createMessage(aiMessage);
                        log.info("AI回复已保存到数据库，messageId: {}", savedAiMessage.getId());
                        
                        // 保存到 Redis 上下文
                        contextService.saveMessage(sessionId, "assistant", aiResponse);
                        log.info("AI回复已保存到Redis上下文，sessionId: {}", sessionId);
                    }
                } catch (Exception e) {
                    log.error("保存AI回复失败", e);
                }

                // 所有流式分片接收完，再发结束标记、关闭连接
                if (!completed.get()) {
                    emitter.send(SseEmitter.event().data("流式对话完成"));
                    emitter.complete();
                    completed.set(true);
                    log.info("发送流式对话完成消息，关闭SSE连接");
                }
            } catch (NumberFormatException e) {
                log.error("sessionId 格式错误: {}", request.getSessionId(), e);
                try {
                    emitter.send(SseEmitter.event().data("sessionId 格式错误，必须是数字"));
                    emitter.complete();
                    completed.set(true);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            } catch (Exception e) {
                log.error("流式对话整体处理异常", e);
                if (!completed.get()) {
                    emitter.completeWithError(e);
                    completed.set(true);
                }
            }
        }).start();

        return emitter;
    }

    /**
     * 抽离：创建并完整配置 SseEmitter
     */
    private SseEmitter createConfiguredSseEmitter() {
        // 超时120秒
        SseEmitter emitter = new SseEmitter(120000L) {
            @Override
            protected void extendResponse(@NotNull ServerHttpResponse outputMessage) {
                super.extendResponse(outputMessage);
                // 标准SSE响应头
                outputMessage.getHeaders().set("Cache-Control", "no-cache");
                outputMessage.getHeaders().set("Connection", "keep-alive");
                outputMessage.getHeaders().set("Content-Type", "text/event-stream; charset=UTF-8");
            }
        };

        // 连接完成回调
        emitter.onCompletion(() -> log.info("SSE Emitter 连接已完成关闭"));
        // 超时回调
        emitter.onTimeout(() -> {
            log.warn("SSE Emitter 连接超时");
            emitter.complete();
        });
        // 异常回调
        emitter.onError(ex -> log.error("SSE Emitter 连接异常", ex));

        return emitter;
    }
}