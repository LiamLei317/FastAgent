package com.fast.agent.core.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置类
 */
@Configuration
public class LangChain4jConfig {

    /**
     * 配置 ChatLanguageModel Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = "langchain4j.open-ai", name = "api-key")
    public ChatLanguageModel chatLanguageModel(com.fast.agent.core.llm.LlmConfig llmConfig) {
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
}
