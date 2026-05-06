package com.fast.agent.core.agent.base;

import lombok.Data;

/**
 * Agent 抽象基类
 */
@Data
public abstract class BaseAgent {

    /**
     * Agent 名称
     */
    protected String name;

    /**
     * Agent 描述
     */
    protected String description;

    /**
     * 系统提示词
     */
    protected String systemPrompt;

    /**
     * 执行任务
     */
    public abstract String execute(String input);
}
