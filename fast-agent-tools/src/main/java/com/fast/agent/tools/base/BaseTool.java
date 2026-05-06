package com.fast.agent.tools.base;

import lombok.Data;

/**
 * 工具基类
 */
@Data
public abstract class BaseTool {

    /**
     * 工具名称
     */
    protected String name;

    /**
     * 工具描述
     */
    protected String description;

    /**
     * 执行工具
     */
    public abstract String execute(String input);
}
