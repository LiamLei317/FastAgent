package com.fast.agent.web.controller;

import com.fast.agent.core.flow.StepCompletionDetector;
import com.fast.agent.core.intent.IntentParser;
import com.fast.agent.core.session.SessionStepRedisService;
import com.fast.agent.core.skills.IntentBasedSkillPromptLoader;
import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.model.entity.Message;
import com.fast.agent.model.entity.Session;
import com.fast.agent.model.enums.IntentType;
import com.fast.agent.model.enums.SessionStep;
import com.fast.agent.service.ChatService;
import com.fast.agent.service.ConversationContextService;
import com.fast.agent.service.MessageService;
import com.fast.agent.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流式对话控制器 - 意图驱动 + 流程状态引导
 * 
 * 核心架构：
 * 1. 每轮对话必须先执行意图识别
 * 2. 根据识别出的意图加载对应技能Prompt
 * 3. Redis仅存储流程状态，做友好引导不限制用户
 * 4. 支持用户随时重问、质疑、跑偏闲聊
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/mf")
@RequiredArgsConstructor
public class StreamingChatControllerMF {

    private final ChatService chatService;
    private final SessionService sessionService;
    private final MessageService messageService;
    private final ConversationContextService contextService;
    private final IntentParser intentParser;
    private final IntentBasedSkillPromptLoader skillPromptLoader;
    private final SessionStepRedisService sessionStepRedisService;
    private final StepCompletionDetector stepCompletionDetector;

    /**
     * 构建完整消息：技能SystemPrompt → 流程引导提示 → 历史对话 → 用户问题
     * @param skillPrompt 技能Prompt
     * @param currentStep 当前流程步骤
     * @param context 历史对话上下文
     * @param userQuestion 用户当前问题
     * @return 完整消息
     */
    private String buildFullMessage(String skillPrompt, SessionStep currentStep, String context, String userQuestion) {
        StringBuilder fullMessage = new StringBuilder();
        
        // 1. SystemMessage(技能Prompt)
        fullMessage.append("系统指令：").append(skillPrompt).append("\n\n");
        
        // 2. 流程引导提示（仅做引导，不强制）
        String stepHint = currentStep.getFriendlyHint();
        fullMessage.append("流程引导：").append(stepHint).append("\n\n");
        
        // 3. Redis历史对话（如果有）
        if (context != null && !context.trim().isEmpty()) {
            fullMessage.append("历史对话：").append(context).append("\n\n");
        }
        
        // 4. UserMessage(当前问题)
        fullMessage.append("用户问题：").append(userQuestion);
        
        return fullMessage.toString();
    }

    /**
     * 流式对话接口 - 意图驱动 + 状态引导
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request) {
        // 创建并配置SseEmitter
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
                
                // 步骤1：执行意图识别（必须优先）
                IntentType intentType = intentParser.parseIntent(request.getMessage());
                log.info("识别到用户意图：{}", intentType);
                log.info("用户问题命中意图：{} (编码：{})", intentType, intentType.getCode());
                
                // 步骤2：根据意图加载对应的技能Prompt
                String skillPrompt = skillPromptLoader.getSkillPrompt(intentType);
                log.info("加载意图技能Prompt成功，意图：{}，Prompt长度：{}", intentType, skillPrompt.length());
                
                // 步骤3：从Redis读取当前流程状态（仅用于引导）
                SessionStep currentStep = sessionStepRedisService.getSessionStep(sessionId);
                log.info("会话{}当前流程步骤：{}", sessionId, currentStep);
                
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
                
                // 步骤4：获取最近5轮对话上下文
                String context = contextService.formatContext(sessionId, 5);
                
                // 步骤5：拼接消息：技能Prompt → 流程引导 → 历史对话 → 用户问题
                String fullMessage = buildFullMessage(skillPrompt, currentStep, context, request.getMessage());
                
                // 创建包含完整上下文的请求
                ChatRequest contextRequest = new ChatRequest();
                contextRequest.setSessionId(sessionId);
                contextRequest.setMessage(fullMessage);
                
                // 打印完整消息内容
                log.info("=== 流式对话 - 发送给大模型的完整消息 ===");
                log.info("sessionId: {}", sessionId);
                log.info("识别意图: {}", intentType);
                log.info("当前流程步骤: {}", currentStep);
                log.info("原始消息: {}", request.getMessage());
                log.info("上下文长度: {} 字符", context.length());
                log.info("完整消息长度: {} 字符", fullMessage.length());
                log.info("完整消息内容: {}", fullMessage);
                log.info("============================================");
                
                // 发送连接成功标识
                emitter.send(SseEmitter.event().data("连接成功"));
                log.info("发送连接成功消息，意图：{}，当前步骤：{}", intentType, currentStep);

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

                // AI回复完成后，处理阶段推进逻辑
                String aiResponse = fullResponse.toString();
                if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                    // 清理完成标记后的内容用于保存
                    String cleanedResponse = stepCompletionDetector.cleanCompletionMarkers(aiResponse);
                    
                    // 保存AI回复到数据库和Redis
                    try {
                        Message aiMessage = new Message();
                        aiMessage.setSessionId(Long.parseLong(sessionId));
                        aiMessage.setRole("assistant");
                        aiMessage.setContent(cleanedResponse);
                        aiMessage.setCreateTime(java.time.LocalDateTime.now());
                        
                        Message savedAiMessage = messageService.createMessage(aiMessage);
                        log.info("AI回复已保存到数据库，messageId: {}", savedAiMessage.getId());
                    } catch (Exception e) {
                        log.error("保存AI回复失败", e);
                    }
                    
                    // 保存AI回复到 Redis 上下文
                    contextService.saveMessage(sessionId, "assistant", cleanedResponse);
                    
                    // 步骤6：检测当前阶段是否完成，如果完成则自动推进到下一步
                    if (stepCompletionDetector.isStepCompleted(aiResponse)) {
                        log.info("检测到阶段完成标记，准备推进到下一步");
                        
                        SessionStep nextStep = sessionStepRedisService.proceedToNextStep(sessionId);
                        log.info("会话{}步骤推进成功：{} -> {}", sessionId, currentStep, nextStep);
                        
                        // 发送阶段推进通知
                        if (nextStep.isFinished()) {
                            emitter.send(SseEmitter.event().data("🎉 产品创意全流程已完成！您可以继续讨论任何相关问题。"));
                            log.info("会话{}全流程完成", sessionId);
                        } else {
                            String nextHint = nextStep.getFriendlyHint();
                            emitter.send(SseEmitter.event().data("✅ 当前阶段已完成，建议进入下一阶段：" + nextStep));
                            emitter.send(SseEmitter.event().data("💡 " + nextHint));
                            log.info("会话{}进入下一阶段：{} - {}", sessionId, nextStep, nextHint);
                        }
                    }
                }

                // 正常完成流式对话
                emitter.complete();
                completed.set(true);
                log.info("流式对话正常完成，sessionId: {}, 意图：{}, 当前步骤: {}", sessionId, intentType, currentStep);

            } catch (Exception e) {
                if (!completed.get()) {
                    try {
                        emitter.send(SseEmitter.event().data("对话过程中发生错误: " + e.getMessage()));
                        emitter.complete();
                        completed.set(true);
                    } catch (Exception ex) {
                        log.error("SSE 错误通知失败", ex);
                        emitter.completeWithError(ex);
                    }
                    log.error("流式对话异常", e);
                }
            }
        }).start();

        return emitter;
    }

    /**
     * 创建并配置SseEmitter
     * @return 配置好的SseEmitter
     */
    private SseEmitter createConfiguredSseEmitter() {
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        emitter.onCompletion(() -> log.debug("SSE连接完成"));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });
        emitter.onError((throwable) -> log.error("SSE连接发生错误", throwable));
        return emitter;
    }
}
