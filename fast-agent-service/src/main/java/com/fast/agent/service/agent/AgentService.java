package com.fast.agent.service.agent;

import com.fast.agent.core.agent.base.BaseAgent;

/**
 * Agent 服务
 */
public interface AgentService {

    /**
     * 获取 Agent
     */
    BaseAgent getAgent(String agentName);

    /**
     * 执行 Agent
     */
    String executeAgent(String agentName, String input);
}
