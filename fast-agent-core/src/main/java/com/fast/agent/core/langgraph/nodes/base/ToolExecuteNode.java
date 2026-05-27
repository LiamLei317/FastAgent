package com.fast.agent.core.langgraph.nodes.base;

import com.fast.agent.common.constant.BaseNodeConstant;
import com.fast.agent.core.langgraph.state.base.FastAgentState;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
public class ToolExecuteNode {
    public FastAgentState execute(FastAgentState state) {
        return state
                .withNeedToolCall(false)
                .withToolAllDone(true);
    }
}