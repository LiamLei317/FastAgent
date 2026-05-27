package com.fast.agent.core.langgraph.nodes.base;

import com.fast.agent.common.constant.BaseNodeConstant;
import com.fast.agent.core.langgraph.state.base.FastAgentState;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.action.AsyncNodeActionWithConfig;
import org.bsc.langgraph4j.internal.node.Node;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ThinkNode {

    public FastAgentState execute(FastAgentState state) {
        return state.withThought("LLM 思考中：" + state.getUserQuery());
    }
}
