package com.fast.agent.core.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LLM 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmConfig llmConfig;

    /**
     * 获取 ChatLanguageModel 实例
     */
    public ChatLanguageModel getChatModel() {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(llmConfig.getApiKey())
                .modelName(llmConfig.getModelName())
                .temperature(llmConfig.getTemperature())
                .maxTokens(llmConfig.getMaxTokens())
                .timeout(java.time.Duration.ofSeconds(llmConfig.getTimeout()));
        if (llmConfig.getBaseUrl() != null && !llmConfig.getBaseUrl().isEmpty()) {
            builder.baseUrl(llmConfig.getBaseUrl());
        }
        return builder.build();
    }

    /**
     * 发送消息并获取回复
     */
    public String chat(String message) {
        ChatLanguageModel model = getChatModel();
        return model.generate(message);
    }
}
