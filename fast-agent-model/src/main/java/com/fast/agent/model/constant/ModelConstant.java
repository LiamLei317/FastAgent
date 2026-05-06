package com.fast.agent.model.constant;

/**
 * 模型常量
 */
public class ModelConstant {

    /**
     * 默认模型
     */
    public static final String DEFAULT_MODEL = "gpt-3.5-turbo";

    /**
     * 默认温度
     */
    public static final Double DEFAULT_TEMPERATURE = 0.7;

    /**
     * 默认最大Token数
     */
    public static final Integer DEFAULT_MAX_TOKENS = 2000;

    /**
     * 系统提示词前缀
     */
    public static final String SYSTEM_PROMPT_PREFIX = "You are a helpful AI assistant.";

    private ModelConstant() {
    }
}
