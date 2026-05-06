package com.fast.agent.service.agent.impl;

import com.fast.agent.core.agent.base.BaseAgent;
import com.fast.agent.core.agent.react.ReactAgent;
import com.fast.agent.service.agent.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Agent 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final ReactAgent reactAgent;

    @Override
    public BaseAgent getAgent(String agentName) {
        return switch (agentName) {
            case "react" -> reactAgent;
            default -> null;
        };
    }

    @Override
    public String executeAgent(String agentName, String input) {
        BaseAgent agent = getAgent(agentName);
        if (agent == null) {
            log.error("Agent 不存在: agentName={}", agentName);
            return "Agent 不存在: " + agentName;
        }
        return agent.execute(input);
    }
}
