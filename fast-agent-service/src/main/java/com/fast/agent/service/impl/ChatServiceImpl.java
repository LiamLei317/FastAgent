package com.fast.agent.service.impl;

import com.fast.agent.common.enums.ChatRole;
import com.fast.agent.common.utils.AIMessageUtils;
import com.fast.agent.core.intent.IntentParser;
import com.fast.agent.core.skills.SkillsPromptLoader;
import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.model.entity.Message;
import com.fast.agent.model.entity.Session;
import com.fast.agent.model.enums.IntentType;
import com.fast.agent.service.ChatService;
import com.fast.agent.service.MessageService;
import com.fast.agent.service.SessionService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import dev.langchain4j.data.message.SystemMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 流式对话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ThreadPoolExecutor asyncExecutor;
    private final SessionService sessionService;
    private final IntentParser intentParser;
    private final SkillsPromptLoader skillPromptLoader;
    private final StreamingChatLanguageModel streamingChatModel;
    private final MessageService messageService;

    private final Map<String, ChatMemory> chatMemoryMap = new ConcurrentHashMap<>();

    // 最大 token 数（根据模型自己调整）
    private static final int MAX_TOKEN = 4096;

    @Override
    public void streamChat(ChatRequest request, SseEmitter sseEmitter) {

        asyncExecutor.execute(() -> {
            String sessionId = request.getSessionId();

            try {
                // 会话校验
                Session session = sessionService.getSessionById(sessionId);
                if (session == null) {
                    sseEmitter.send(SseEmitter.event()
                            .data("会话不存在，sessionId: " + sessionId));
                    sseEmitter.complete(); // 关闭连接
                    return;
                }
                log.info("============================ 开始对话");
                // 意图识别
                IntentType intentType = intentParser.parseIntent(request.getMessage());
                // 根据意图获取prompt
                String skillPrompt = skillPromptLoader.getSkillPrompt(intentType);
                // todo 本来想用 tokneWindowChatMemory 但是找不到能用的分词器，后续要处理
                ChatMemory chatMemory = chatMemoryMap.computeIfAbsent(sessionId,
                        k -> MessageWindowChatMemory.withMaxMessages(6)
                );

                // 构建流式 AI 服务
                StreamingChatAssistant aiService = AiServices.builder(StreamingChatAssistant.class)
                        .chatMemory(chatMemory)
                        .streamingChatLanguageModel(streamingChatModel)
                        .build();

                // 保存用户消息
                saveMessage(sessionId, request.getMessage(), ChatRole.USER.name());

                // 流式调用
                StringBuilder fullAiResponse = new StringBuilder();

                TokenStream tokenStream = aiService.stream(
                        skillPrompt,
                        request.getMessage()
                );

                // 开始对话
                tokenStream.onNext(token -> {
                            try {
                                sseEmitter.send(SseEmitter.event().data(token));
                                fullAiResponse.append(token);
                            } catch (Exception e) {
                                log.error("SSE 推送失败", e);
                            }
                        })
                        .onComplete(response -> {
                            saveMessage(sessionId, fullAiResponse.toString(), ChatRole.ASSISTANT.name());
                            sseEmitter.complete();
                            log.info("流式对话完成");
                        })
                        .onError(e -> {
                            log.error("流式对话异常", e);
                            try {
                                sseEmitter.send(SseEmitter.event().data("对话异常：" + e.getMessage()));
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                            sseEmitter.completeWithError(e);
                        })
                        .start();


            } catch (Exception e) {
                log.error("对话异常", e);
                try {
                    sseEmitter.send(SseEmitter.event().data("系统异常：" + e.getMessage()));
                } catch (Exception ex) {
                    log.error("发送错误失败", ex);
                } finally {
                    sseEmitter.complete();
                }
            }
        });
    }

    private interface StreamingChatAssistant {
        TokenStream stream(@V("systemMessage") String  systemMessage,
                           @UserMessage String userMessage);
    }

    private void saveMessage(String sessionId, String content, String role) {
        try {
            Message msg = new Message();
            msg.setSessionId(sessionId);
            msg.setRole(role);
            msg.setContent(content);
            messageService.createMessage(msg);
        } catch (Exception e) {
            log.error("保存用户消息失败", e);
        }
    }
}
