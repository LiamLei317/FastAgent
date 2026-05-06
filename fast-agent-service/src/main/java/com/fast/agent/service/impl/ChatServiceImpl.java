package com.fast.agent.service.impl;

import com.fast.agent.core.llm.LlmConfig;
import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.service.ChatService;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 流式对话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final LlmConfig llmConfig;

    @Override
    public void streamChat(ChatRequest request, Consumer<String> chunkConsumer) {
        try {
            OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                    .apiKey(llmConfig.getApiKey())
                    .modelName(llmConfig.getModelName())
                    .temperature(llmConfig.getTemperature())
                    .maxTokens(llmConfig.getMaxTokens())
                    .timeout(java.time.Duration.ofSeconds(60));
            if (llmConfig.getBaseUrl() != null && !llmConfig.getBaseUrl().isEmpty()) {
                builder.baseUrl(llmConfig.getBaseUrl());
            }
            OpenAiStreamingChatModel model = builder.build();

            StreamingChatAssistant assistant = AiServices.builder(StreamingChatAssistant.class)
                    .streamingChatLanguageModel(model)
                    .build();

            // 使用 CountDownLatch 确保流式处理完成
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            
            // 打印发送给大模型的消息内容
            String messageToSend = request.getMessage();
            log.info("=== 发送给大模型的消息 ===");
            log.info("消息长度: {} 字符", messageToSend.length());
            log.info("消息内容: {}", messageToSend);
            log.info("==========================");
            
            TokenStream tokenStream = assistant.chat(messageToSend);
            tokenStream.onNext(chunk -> {
                log.info("接收到流式数据: {}", chunk);
                chunkConsumer.accept(chunk);
            })
                    .onComplete(response -> {
                        log.info("流式对话完成，最终响应: {}", response);
                        latch.countDown();
                    })
                    .onError(error -> {
                        log.error("流式对话异常", error);
                        latch.countDown();
                    })
                    .start();

            // 等待流式处理完成，确保不会提前返回
            try {
                latch.await(120, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("等待流式处理完成时被中断", e);
                Thread.currentThread().interrupt();
            }

        } catch (Exception e) {
            log.error("流式对话失败", e);
            throw new RuntimeException("流式对话失败", e);
        }
    }

    interface StreamingChatAssistant {
        TokenStream chat(String message);
    }
}
