package com.fast.agent.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息角色枚举
 */
@Getter
@AllArgsConstructor
public enum MessageRoleEnum {

    /**
     * 用户
     */
    USER("user", "用户"),

    /**
     * 助手
     */
    ASSISTANT("assistant", "助手"),

    /**
     * 系统
     */
    SYSTEM("system", "系统");

    private final String code;
    private final String desc;
}
