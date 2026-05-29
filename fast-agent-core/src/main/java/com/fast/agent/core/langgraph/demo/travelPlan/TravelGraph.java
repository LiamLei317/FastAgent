package com.fast.agent.core.langgraph.demo.travelPlan;

import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.LG4JLoggable.log;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * Supervisor-worker 模式
 * 所有的 worker 执行完后都会 handoff 给 supervisor
 * supervisor 负责决定下一步执行哪一个 agent
 */
@Component
@AllArgsConstructor
public class TravelGraph {

    private final TravelSupervisorNode supervisor;
    private final SpotAgentNode spotAgent;
    private final FoodAgentNode foodAgent;
    private final HotelAgentNode hotelAgent;

    public Map<String, Object> execute(String userInput) throws GraphStateException {

        StateGraph<TravelState> graph = new StateGraph<>(TravelState::new)
                .addNode("supervisor", supervisor)
                .addNode("spot_agent", spotAgent)
                .addNode("food_agent", foodAgent)
                .addNode("hotel_agent", hotelAgent)

                .addWrapCallNodeHook(new TraceNodeHook()) // 全局钩子

                .addEdge(START, "supervisor")

                // 条件路由 Handoff
                .addConditionalEdges(
                        "supervisor",
                        state -> CompletableFuture.completedFuture(
                                state.handoffTarget().orElse(END)
                        ),
                        Map.of(
                                "spot_agent", "spot_agent",
                                "food_agent", "food_agent",
                                "hotel_agent", "hotel_agent",
                                END, END
                        )
                )
                .addEdge("spot_agent", "supervisor")
                .addEdge("food_agent", "supervisor")
                .addEdge("hotel_agent", "supervisor");

        CompiledGraph<TravelState> compiledGraph = graph.compile();

        Map<String, Object> inputs = Map.of("userDemand", userInput);
        compiledGraph.invoke(inputs);
        return inputs;
    }
}