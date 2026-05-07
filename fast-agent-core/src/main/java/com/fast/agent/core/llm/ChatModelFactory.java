package com.fast.agent.core.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChatModelFactory {

    private final LlmConfig llmConfig;

    public ChatLanguageModel createChatModel() {
        return switch (llmConfig.getModelType()) {
            case OPENAI -> createOpenAiModel();
            case ZHI_PU -> createZhiPuModel();
            case DEEPSEEK -> createDeepSeekModel();
            default -> throw new RuntimeException("不支持的模型类型");
        };
    }

    private ChatLanguageModel createOpenAiModel() {
        return OpenAiChatModel.builder()
                .apiKey(llmConfig.getApiKey())
                .baseUrl(llmConfig.getBaseUrl())
                .modelName(llmConfig.getModelName())
                .temperature(0.1)
                .build();
    }

    private ChatLanguageModel createZhiPuModel() {
        return ZhipuAiChatModel.builder()
                .apiKey(llmConfig.getApiKey())
                .temperature(0.1)
                .callTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .connectTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .writeTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .readTimeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .build();
    }

    private ChatLanguageModel createDeepSeekModel() {
        // DeepSeek 用 OpenAI 兼容协议
        return OpenAiChatModel.builder()
                .apiKey(llmConfig.getApiKey())
                .baseUrl(llmConfig.getBaseUrl())
                .modelName(llmConfig.getModelName())
                .temperature(0.1)
                .build();
    }
}
