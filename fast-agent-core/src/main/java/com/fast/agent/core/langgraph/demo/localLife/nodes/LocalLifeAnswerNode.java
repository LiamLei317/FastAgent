package com.fast.agent.core.langgraph.demo.localLife.nodes;


import com.fast.agent.core.langgraph.demo.localLife.LocalLifeState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component("localLifeAnswerNode")
@Slf4j
public class LocalLifeAnswerNode implements DynamicNode {

    @Override
    public String name() {
        return "answer";
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(LocalLifeState state) {
        Map<String, String> toolResults = state.toolResults();
        String finalAnswer = state.finalAnswer().orElse("");

        StringBuilder sb = new StringBuilder();
        if (!toolResults.isEmpty()) {
            toolResults.forEach((k, v) -> sb.append(k).append("：").append(v).append("\n"));
        }
        sb.append(finalAnswer);
        log.info("final answer: {}", sb);
        return CompletableFuture.completedFuture(Map.of(
                LocalLifeState.FINAL_ANSWER, sb.toString()
        ));
    }
}