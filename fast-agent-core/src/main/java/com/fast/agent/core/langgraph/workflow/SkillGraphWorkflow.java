package com.fast.agent.core.langgraph.workflow;

import com.fast.agent.core.langgraph.nodes.*;
import com.fast.agent.core.langgraph.state.SkillGraphState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Component
@RequiredArgsConstructor
public class SkillGraphWorkflow {

    private final AtomicSkillNode atomicNode;
    private final LoadBusinessNode loadBusinessNode;
    private final SelectBusinessNode selectBusinessNode;
    private final BuildPromptNode buildPromptNode;
    private final GenerateAnswerNode generateAnswerNode;
    private final AnswerCheckNode checkNode;

    @Bean
    public CompiledGraph<SkillGraphState> skillGraph() throws GraphStateException {
        AgentStateFactory<SkillGraphState> stateFactory = SkillGraphState::new;
        StateGraph<SkillGraphState> graph = new StateGraph<>(stateFactory);

        graph.addNode("atomic",          state -> executeNode(atomicNode::execute, state));
        graph.addNode("load_business",   state -> executeNode(loadBusinessNode::execute, state));
        graph.addNode("select_business", state -> executeNode(selectBusinessNode::execute, state));
        graph.addNode("build_prompt",    state -> executeNode(buildPromptNode::execute, state));
        graph.addNode("generate",        state -> executeNode(generateAnswerNode::execute, state));
        graph.addNode("check",           state -> executeNode(checkNode::execute, state));

        graph.addEdge(START, "atomic");
        graph.addEdge("atomic", "load_business");
        graph.addEdge("load_business", "select_business");
        graph.addEdge("select_business", "build_prompt");
        graph.addEdge("build_prompt", "generate");
        graph.addEdge("generate", "check");

        graph.addConditionalEdges("check", this::resolveRouter, Map.of(
                "PASS", END,
                "FORMAT_ERROR", "build_prompt",
                "BUSINESS_MISMATCH", "select_business",
                "INTENT_ERROR", "atomic"
        ));

        return graph.compile();
    }

    // 节点执行（带异常捕获）
    private CompletableFuture<Map<String, Object>> executeNode(
            java.util.function.Function<SkillGraphState, SkillGraphState> nodeFunc,
            SkillGraphState state
    ) {
        try {
            SkillGraphState newState = nodeFunc.apply(state);
            return CompletableFuture.completedFuture(newState.data());
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
            f.completeExceptionally(e);
            return f;
        }
    }

    private CompletableFuture<String> resolveRouter(SkillGraphState state) {
        try {
            if (state.getRetryCount() != null && state.getRetryCount() >= 3) {
                return CompletableFuture.completedFuture("PASS");
            }
            return CompletableFuture.completedFuture(state.getCheckResult());
        } catch (Exception e) {
            CompletableFuture<String> f = new CompletableFuture<>();
            f.completeExceptionally(e);
            return f;
        }
    }
}