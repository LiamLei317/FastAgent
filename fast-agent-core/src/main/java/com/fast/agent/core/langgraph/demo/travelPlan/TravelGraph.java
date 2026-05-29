package com.fast.agent.core.langgraph.demo.travel;

import com.fast.agent.core.langgraph.demo.travelPlan.*;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Component
@AllArgsConstructor
public class TravelGraph {

    private final TravelSupervisorNode supervisor;
    private final SpotAgentNode spotAgent;
    private final FoodAgentNode foodAgent;
    private final HotelAgentNode hotelAgent;

    // ✅ 完全对齐你的 WeatherAskGraph 风格
    public Map<String, Object> execute(String userInput) throws GraphStateException {

        // ✅ 最新正确写法：不需要 StateSchema
        StateGraph<TravelState> graph = new StateGraph<>(TravelState::new)
                .addNode("supervisor", supervisor)
                .addNode("spot_agent", spotAgent)
                .addNode("food_agent", foodAgent)
                .addNode("hotel_agent", hotelAgent)

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
        Optional<TravelState> result = compiledGraph.invoke(inputs);

        // 输出日志（和你天气Demo格式一样）
        System.out.println("===== 旅行规划智能体执行结果 =====");
        result.ifPresent(state -> {
            System.out.println("执行计划：" + state.executePlan().orElse("无"));
            System.out.println("最终攻略：\n" + state.finalTravelGuide().orElse("无"));
        });

        return inputs;
    }
}