package com.fast.agent.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型类型枚举
 */
@Getter
@AllArgsConstructor
public enum ModelTypeEnum {

    /**
     * GPT-3.5
     */
    GPT_3_5("gpt-3.5-turbo", "GPT-3.5 Turbo"),

    /**
     * GPT-4
     */
    GPT_4("gpt-4", "GPT-4"),

    /**
     * GPT-4-Turbo
     */
    GPT_4_TURBO("gpt-4-turbo", "GPT-4 Turbo"),

    /**
     * Claude-3
     */
    CLAUDE_3("claude-3-opus", "Claude-3 Opus"),

    /**
     * 通义千问
     */
    QWEN("qwen-turbo", "通义千问 Turbo"),

    /**
     * 文心一言
     */
    ERNIE("ernie-bot", "文心一言");

    private final String code;
    private final String desc;
}
