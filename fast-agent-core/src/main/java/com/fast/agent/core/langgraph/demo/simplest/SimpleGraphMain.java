package com.fast.agent.core.langgraph.demo.simplest;

import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Component
@AllArgsConstructor
public class SimpleGraphMain {

    private final UserInputNode userInputNode;
    private final AiResponseNode aiResponseNode;

    public Map<String, Object> execute(String userInput) throws GraphStateException {
        StateGraph<ChatState> graph = new StateGraph<>(ChatState.SCHEMA, ChatState::new)
                .addNode("userInput", userInputNode)
                .addNode("aiResponse", aiResponseNode)
                .addEdge(START, "userInput")
                .addEdge("userInput", "aiResponse")
                .addEdge("aiResponse", END);

        // 编译
        var compiled = graph.compile();

        Map<String, Object> inputs = Map.of(
                "userInput", userInput  // 传给 Node 的参数
        );

        Optional<ChatState> resultOpt = compiled.invoke(inputs);

        // 输出
        System.out.println("==== 对话记录 ====");
        resultOpt.ifPresent(state ->
                state.messages().forEach(System.out::println)
        );
        return inputs;
    }
}
