package com.fast.agent.core.langgraph.demo.localLife;

import com.fast.agent.core.langgraph.demo.localLife.nodes.*;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Component
@AllArgsConstructor
public class LocalLifeGraph {

    private final PlannerNode planner;
    private final DecisionNode decision;
    private final ToolNode tool;
    private final LocalLifeAnswerNode answer;
    private final MemoryNode memory;

    public Map<String, Object> execute(String userInput) throws GraphStateException {

        StateGraph<LocalLifeState> graph = new StateGraph<>(LocalLifeState::new)
                .addNode("planner", planner)
                .addNode("decision", decision)
                .addNode("tool", tool)
                .addNode("answer", answer)
                .addNode("memory", memory)

                .addEdge(START, "planner")
                .addEdge("planner", "decision")
                .addConditionalEdges(
                        "decision",
                        state -> {
                            if (state.isNeedsTool()) {
                                return CompletableFuture.completedFuture("tool");
                            }
                            return CompletableFuture.completedFuture("answer");
                        },
                        Map.of(
                                "tool", "tool",
                                "answer", "answer",
                                END, END
                        )
                )

                .addEdge("tool", "decision")
                .addEdge("answer", "memory")
                .addEdge("memory", END);

        CompiledGraph<LocalLifeState> compiledGraph = graph.compile();
        Map<String, Object> inputs = Map.of(LocalLifeState.USER_INPUT, userInput);
        compiledGraph.invoke(inputs);

        return inputs;
    }
}