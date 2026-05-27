package com.fast.agent.core.langgraph.workflow.base;

import com.fast.agent.common.constant.BaseNodeConstant;
import com.fast.agent.core.langgraph.nodes.*;
import com.fast.agent.core.langgraph.nodes.base.AnswerNode;
import com.fast.agent.core.langgraph.nodes.base.SkillNode;
import com.fast.agent.core.langgraph.nodes.base.ThinkNode;
import com.fast.agent.core.langgraph.nodes.base.ToolExecuteNode;
import com.fast.agent.core.langgraph.state.base.FastAgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Component
@RequiredArgsConstructor
public class FastAgentWorkflow {
    private final CompiledGraph<FastAgentState> fastAgentGraph;

    public FastAgentState run(String userQuery) throws Exception {
        Map<String, Object> initData = Map.of("userQuery", userQuery);
        Optional<FastAgentState> result = fastAgentGraph.invoke(initData);
        return result.orElseThrow();
    }
}