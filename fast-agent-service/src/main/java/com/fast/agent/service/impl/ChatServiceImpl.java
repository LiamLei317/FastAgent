package com.fast.agent.service.impl;

import com.fast.agent.common.enums.ChatRole;
import com.fast.agent.common.utils.SkillUtil;
import com.fast.agent.common.utils.SystemPromptUtil;
import com.fast.agent.core.intent.IntentParser;
import com.fast.agent.core.skills.SkillsPromptLoader;
import com.fast.agent.memory.shortterm.CustomRedisChatMemoryStore;
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
    private final CustomRedisChatMemoryStore customRedisChatMemoryStore;


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
                String userInput = request.getMessage();
                // 意图识别
                IntentType intentType = intentParser.parseIntent(userInput);
                // 根据意图获取prompt
                String skillPrompt = skillPromptLoader.getSkillPrompt(intentType);
                String globalSystem = SystemPromptUtil.get();
                String outputFormatSkill = SkillUtil.buildFullSkill("DOCUMENT_OUTPUT", userInput);

                // ====================== 最终超级System（融合你+我） ======================
                String finalSystem =
                        globalSystem          // 全局规则（我）
                                + "\n\n=== 业务场景技能 ===\n"
                                + skillPrompt   // 你的专业场景Skill（你原来的，不动）
                                + "\n\n=== 输出格式要求 ===\n"
                                + outputFormatSkill;
                // todo 本来想用 tokenWindowChatMemory 但是找不到能用的分词器，后续要处理
                ChatMemory chatMemory = MessageWindowChatMemory.builder()
                        .id(sessionId)                // 用 sessionId 作为记忆ID
                        .maxMessages(6)               // 最多保留最近6条消息
                        .chatMemoryStore(customRedisChatMemoryStore)  // 使用自己的Redis存储
                        .build();

                // 构建流式 AI 服务
                AiServices<StreamingChatAssistant> builder = AiServices.builder(StreamingChatAssistant.class)
                        .streamingChatLanguageModel(streamingChatModel);
                // langchain4j 不允许构建空 memory，所以要提前判断
                if (!chatMemory.messages().isEmpty()) {
                    builder.chatMemory(chatMemory);
                }
                StreamingChatAssistant aiService = builder.build();
                // langchain4j 函数式接口初始化
                TokenStream tokenStream = aiService.stream(
                        finalSystem,
                        request.getMessage()
                );
                // AI 回答
                StringBuilder fullAiResponse = new StringBuilder();
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
                            // 保存用户消息
                            saveMessage(sessionId, request.getMessage(), ChatRole.USER.name());
                            // 保存 ai 回答
                            saveMessage(sessionId, fullAiResponse.toString(), ChatRole.ASSISTANT.name());
                            sseEmitter.complete();
                            log.info("============================ 对话完成");
                        })
                        .onError(e -> {
                            try {
                                sseEmitter.send(SseEmitter.event().data("对话异常：" + e.getMessage()));
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                            sseEmitter.completeWithError(e);
                            log.info("============================ 对话失败");
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
