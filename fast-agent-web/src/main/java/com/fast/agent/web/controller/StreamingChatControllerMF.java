package com.fast.agent.web.controller;

import com.fast.agent.common.utils.AIMessageUtils;
import com.fast.agent.common.utils.SseEmitterUtils;
import com.fast.agent.core.chat.ChatAssistant;
import com.fast.agent.core.intent.IntentParser;
import com.fast.agent.core.skills.IntentBasedSkillPromptLoader;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流式对话控制器 - 意图驱动
 * 
 * 核心架构：
 * 1. 每轮对话必须先执行意图识别
 * 2. 根据识别出的意图加载对应技能Prompt
 * 3. 使用 LangChain4j ChatMemory 自动管理上下文
 * 4. 支持用户随时重问、质疑、跑偏闲聊
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/mf")
@RequiredArgsConstructor
public class StreamingChatControllerMF {

    private final SessionService sessionService;
    private final MessageService messageService;
    private final ChatMemoryConversationService chatMemoryConversationService;
    private final IntentParser intentParser;
    private final IntentBasedSkillPromptLoader skillPromptLoader;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request) {
        // 获取全局统一配置的 Emitter
        SseEmitter emitter = SseEmitterUtils.create();
        AtomicBoolean completed = new AtomicBoolean(false);

        // 异步执行大模型流式回调，不阻塞Tomcat主线程
        new Thread(() -> {
            try {
                // 校验 sessionId 是否存在
                String sessionId = request.getSessionId();
                if (StringUtils.isBlank(sessionId)) {
                    emitter.send(SseEmitter.event().data("sessionId 为空"));
                    emitter.complete();
                    completed.set(true);
                    return;
                }
                // 找到 session 对象
                Session session = sessionService.getSessionById(sessionId);
                if (ObjectUtils.isEmpty(session)) {
                    emitter.send(SseEmitter.event().data("会话不存在，sessionId: " + sessionId));
                    emitter.complete();
                    completed.set(true);
                    return;
                }

                log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~流式对话开始，sessionId: {}, message: {}"
                        , sessionId, request.getMessage());
                
                // 执行意图识别（必须优先）
                IntentType intentType = intentParser.parseIntent(request.getMessage());
                
                // 根据意图加载对应的技能Prompt
                String skillPrompt = skillPromptLoader.getSkillPrompt(intentType);
                log.info("加载意图技能Prompt成功，意图：{}", intentType);
                
                // 获取会话专用的 ChatAssistant
                ChatAssistant chatAssistant = chatMemoryConversationService.getChatAssistant(sessionId);
                
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
                
                // 使用 ChatAssistant 进行对话，自动携带上下文
                String fullMessage = AIMessageUtils.buildFullMessage(skillPrompt, request.getMessage());
                
                log.info("=== 流式对话 - 使用 ChatMemory 发送给大模型 ===");
                log.info("sessionId: {}", sessionId);
                log.info("识别意图: {}", intentType);
                log.info("原始消息: {}", request.getMessage());
                log.info("完整消息长度: {} 字符", fullMessage.length());
                log.info("完整消息内容: {}", fullMessage);
                log.info("============================================");
                
                // 发送连接成功标识
                emitter.send(SseEmitter.event().data("连接成功"));

                // 用于收集完整的AI回复内容
                StringBuilder fullResponse = new StringBuilder();

                // 使用 ChatAssistant 进行流式对话，自动携带上下文
                String aiResponse = chatAssistant.streamChatWithSystem(skillPrompt, request.getMessage());
                
                // 模拟流式推送（ChatAssistant 目前返回完整回复）
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

                // AI回复完成后，保存到数据库
                if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                    try {
                        Message aiMessage = new Message();
                        aiMessage.setSessionId(sessionId);
                        aiMessage.setRole("assistant");
                        aiMessage.setContent(aiResponse);
                        aiMessage.setCreateTime(java.time.LocalDateTime.now());
                        
                        Message savedAiMessage = messageService.createMessage(aiMessage);
                        log.info("AI回复已保存到数据库，messageId: {}", savedAiMessage.getId());
                    } catch (Exception e) {
                        log.error("保存AI回复失败", e);
                    }
                    
                    // ChatMemory 自动保存，无需手动操作
                    log.info("AI回复已自动保存到 ChatMemory，sessionId: {}", sessionId);
                }

                // 正常完成流式对话
                emitter.complete();
                completed.set(true);
                log.info("流式对话正常完成，sessionId: {}, 意图：{}", sessionId, intentType);

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
}
