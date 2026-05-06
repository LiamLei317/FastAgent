package com.fast.agent.tools.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局工具注册器
 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, BaseTool> tools = new ConcurrentHashMap<>();

    /**
     * 注册工具
     */
    public void registerTool(BaseTool tool) {
        tools.put(tool.getName(), tool);
        log.info("注册工具: name={}, description={}", tool.getName(), tool.getDescription());
    }

    /**
     * 获取工具
     */
    public BaseTool getTool(String name) {
        return tools.get(name);
    }

    /**
     * 执行工具
     */
    public String executeTool(String name, String input) {
        BaseTool tool = getTool(name);
        if (tool == null) {
            log.error("工具不存在: name={}", name);
            return "工具不存在: " + name;
        }
        return tool.execute(input);
    }

    /**
     * 获取所有工具
     */
    public Map<String, BaseTool> getAllTools() {
        return tools;
    }
}
