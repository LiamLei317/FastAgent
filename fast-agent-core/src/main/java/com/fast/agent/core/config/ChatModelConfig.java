package com.fast.agent.core.config;

import com.fast.agent.core.llm.LlmConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class ChatModelConfig {

    private final LlmConfig llmConfig;

    /**
     * 核心：
     * 这里返回 ChatLanguageModel 接口
     * 但底层实例是 OpenAiChatModel
     * 以后要换模型，只改这里！
     */
    @Bean
    @Primary
    public ChatLanguageModel chatLanguageModelPri() {
        return ZhipuAiChatModel.builder()
                .apiKey(llmConfig.getApiKey())
                .baseUrl(llmConfig.getBaseUrl())
                .temperature(0.1)
                .callTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .connectTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .writeTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .readTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .build();
    }
}
