package com.fast.agent.core.langgraph.demo.queryWeather;

import lombok.AllArgsConstructor;
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
public class WeatherAskGraph {

    private AiJudgeNode aiJudgeNode;
    private WeatherAskInputNode weatherAskInputNode;
    private WeatherAskResponseNode weatherAskResponseNode;
    private WeatherToolNode weatherToolNode;

    public Map<String, Object> execute(String userInput) throws GraphStateException {
        StateGraph<WeatherAskState> graph = new StateGraph<>(WeatherAskState.SCHEMA, WeatherAskState::new)
                .addNode("userInput", weatherAskInputNode)
                .addNode("aiJudge", aiJudgeNode)
                .addNode("weatherTool", weatherToolNode)
                .addNode("aiResponse", weatherAskResponseNode)

                .addEdge(START, "userInput")
                .addEdge("userInput", "aiJudge")

                // 条件路由：是否需要查天气
                .addConditionalEdges(
                        "aiJudge", // source
                        state -> CompletableFuture.completedFuture(
                                // 根据 needWeather 返回 "YES" 或 "NO"
                                state.<Boolean>value("needWeather").orElse(false) ? "YES" : "NO"
                        ),
                        // mappings：key → 目标节点
                        Map.of(
                                "YES", "weatherTool",
                                "NO", "aiResponse"
                        )
                )

                .addEdge("weatherTool", "aiResponse")
                .addEdge("aiResponse", END);

        var compiled = graph.compile();

        // 传入用户问题
        Map<String, Object> inputs = Map.of("userInput", userInput);
        Optional<WeatherAskState> result = compiled.invoke(inputs);

        // 输出最终对话
        System.out.println("==== 天气对话流程 ====");
        result.ifPresent(chatState -> chatState.messages().forEach(System.out::println));

        return inputs;
    }
}
