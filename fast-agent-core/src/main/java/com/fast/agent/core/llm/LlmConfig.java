package com.fast.agent.core.llm;

import com.fast.agent.model.enums.ModelTypeEnum;
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
     * 模型类型
     */
    private ModelTypeEnum modelType;

    /**
     * API Key
     */
    private String apiKey = "test-key";

    /**
     * 基础 URL（兼容智谱AI等第三方 API）
     */
    private String baseUrl = "https://api.openai.com/v1/";

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

    private Long callTimeout;
    
    // 手动添加 getter 方法
    public String getApiKey() {
        return apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public Integer getTimeout() {
        return timeout;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Long getCallTimeout() {
        return callTimeout;
    }
    public void setCallTimeout(Long callTimeout) {
        this.callTimeout = callTimeout;
    }
}
