package com.fast.agent.demos.langgraph.localLife.nodes;


import com.fast.agent.demos.langgraph.localLife.LocalLifeState;
import com.fast.agent.demos.langgraph.localLife.tools.LocalLifeToolRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ToolNode implements DynamicNode {

    private final LocalLifeToolRegistry registry;

    @Override
    public String name() {
        return "tool";
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(LocalLifeState state) {
        List<String> steps = state.steps();
        String toolName = steps.get(0);
        String query = state.userQuery().orElse("");

        String toolResult = registry.runTool(toolName, query);

        return CompletableFuture.completedFuture(Map.of(
                LocalLifeState.TOOL_RESULTS, Map.of(toolName, toolResult),
                LocalLifeState.STEPS, List.of()   // <-- 必须加这一行！
        ));
    }
}