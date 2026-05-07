package com.fast.agent.core.config;

import com.fast.agent.core.llm.LlmConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 */
 @Configuration
public class LangChain4jConfig {

     @Resource
     private LlmConfig llmConfig;

    /**
     * 配置 ChatLanguageModel Bean
     * 只有当配置存在时才创建
     */
    @Bean
    @ConditionalOnProperty(prefix = "langchain4j.open-ai", name = "api-key", havingValue = "test-key", matchIfMissing = false)
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

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return ZhipuAiStreamingChatModel.builder()
                .apiKey(llmConfig.getApiKey())
                .callTimeout(Duration.ofSeconds(llmConfig.getCallTimeout()))
                .connectTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .writeTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .readTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .build();
    }
}
