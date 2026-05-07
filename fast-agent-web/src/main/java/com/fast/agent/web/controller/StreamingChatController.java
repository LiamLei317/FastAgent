package com.fast.agent.web.controller;

import com.fast.agent.core.chat.ChatAssistant;
import com.fast.agent.core.flow.StepCompletionDetector;
import com.fast.agent.core.intent.IntentParser;
import com.fast.agent.core.session.SessionStepRedisService;
import com.fast.agent.core.skills.SkillsPromptLoader;
import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.model.entity.Message;
import com.fast.agent.model.entity.Session;
import com.fast.agent.model.enums.IntentType;
import com.fast.agent.service.ChatMemoryConversationService;
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

    private final SessionService sessionService;
    private final MessageService messageService;
    private final ChatMemoryConversationService chatMemoryConversationService;
    private final IntentParser intentParser;
    private final SkillsPromptLoader skillsPromptLoader;
    private final SessionStepRedisService sessionStepRedisService;
    private final StepCompletionDetector stepCompletionDetector;

    /**
     * 构建完整消息：SystemMessage(技能Prompt) → UserMessage(当前问题)
     * ChatMemory 会自动携带历史对话上下文
     * @param skillPrompt 技能Prompt
     * @param userQuestion 用户当前问题
     * @return 完整消息
     */
    private String buildFullMessage(String skillPrompt, String userQuestion) {
        StringBuilder fullMessage = new StringBuilder();
        
        // 1. SystemMessage(技能Prompt)
        fullMessage.append("系统指令：").append(skillPrompt).append("\n\n");
        
        // 2. UserMessage(当前问题)
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
                
                Session session = sessionService.getSessionById(sessionId);
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
                    userMessage.setSessionId(sessionId);
                    userMessage.setRole("user");
                    userMessage.setContent(request.getMessage());
                    userMessage.setCreateTime(java.time.LocalDateTime.now());
                    
                    Message savedUserMessage = messageService.createMessage(userMessage);
                    log.info("用户消息已保存到数据库，messageId: {}", savedUserMessage.getId());
                } catch (Exception e) {
                    log.error("保存用户消息失败", e);
                    // 即使保存失败也继续处理对话
                }
                
                // 使用 ChatMemory 进行对话，自动携带上下文
                ChatAssistant chatAssistant = chatMemoryConversationService.getChatAssistant(sessionId);
                
                // 步骤3：使用 ChatMemory 进行对话，自动携带上下文
                String fullMessage = buildFullMessage(skillPrompt, request.getMessage());
                
                log.info("=== 流式对话 - 使用 ChatMemory ===");
                log.info("sessionId: {}", sessionId);
                log.info("原始消息: {}", request.getMessage());
                log.info("使用 ChatMemory 自动管理上下文");
                log.info("============================================");
                
                // 发送连接成功标识
                emitter.send(SseEmitter.event().data("连接成功"));
                log.info("发送连接成功消息，使用 ChatMemory");

                // 用于收集完整的AI回复内容
                StringBuilder fullResponse = new StringBuilder();

                // 使用 ChatAssistant 进行流式对话，自动携带上下文
                String aiResponse = chatAssistant.streamChatWithSystem(skillPrompt, request.getMessage());
                
                // 模拟流式推送
                for (int i = 0; i < aiResponse.length(); i += 10) {
                    if (!completed.get()) {
                        try {
                            int end = Math.min(i + 10, aiResponse.length());
                            String chunk = aiResponse.substring(i, end);
                            emitter.send(SseEmitter.event().data(chunk));
                            fullResponse.append(chunk);
                            Thread.sleep(50); // 模拟流式延迟
                        } catch (Exception e) {
                            log.error("SSE 推送分片失败", e);
                            break;
                        }
                    }
                }

                // 保存AI回复到数据库
                try {
                    if (fullResponse.length() > 0) {
                        String finalAiResponse = fullResponse.toString();
                        
                        // 保存到数据库
                        Message aiMessage = new Message();
                        aiMessage.setSessionId(sessionId);
                        aiMessage.setRole("assistant");
                        aiMessage.setContent(finalAiResponse);
                        aiMessage.setCreateTime(java.time.LocalDateTime.now());
                        
                        Message savedAiMessage = messageService.createMessage(aiMessage);
                        log.info("AI回复已保存到数据库，messageId: {}", savedAiMessage.getId());
                        
                        // ChatMemory 自动保存，无需手动操作
                        log.info("AI回复已自动保存到 ChatMemory，sessionId: {}", sessionId);
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