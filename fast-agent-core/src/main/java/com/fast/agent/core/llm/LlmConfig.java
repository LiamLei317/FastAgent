package com.fast.agent.core.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "langchain4j.open-ai")
public class LlmConfig {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 基础 URL（兼容智谱AI等第三方 API）
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String modelName = "gpt-3.5-turbo";

    /**
     * 温度参数
     */
    private Double temperature = 0.7;

    /**
     * 最大Token数
     */
    private Integer maxTokens = 2000;

    /**
     * 超时时间（秒）
     */
    private Integer timeout = 60;
}
