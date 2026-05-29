package com.fast.agent.core.langgraph.workflow.base;

import com.fast.agent.core.langgraph.state.base.FastAgentState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FastAgentWorkflow {

    public FastAgentState run(String userQuery) throws Exception {
        Map<String, Object> initData = Map.of("userQuery", userQuery);
        return new FastAgentState(initData);
    }
}