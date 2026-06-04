package com.fast.agent.demos.langgraph.localLife.nodes;


import com.fast.agent.demos.langgraph.localLife.LocalLifeState;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class MemoryNode implements DynamicNode {

    @Override
    public String name() {
        return "memory";
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(LocalLifeState state) {
        String user = state.userQuery().orElse("");
        String ai = state.finalAnswer().orElse("");

        return CompletableFuture.completedFuture(Map.of(
                LocalLifeState.MEMORY, List.of("用户：" + user, "AI：" + ai)
        ));
    }
}
