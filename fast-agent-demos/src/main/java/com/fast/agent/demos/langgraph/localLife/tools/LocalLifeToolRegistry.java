package com.fast.agent.demos.langgraph.localLife.tools;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LocalLifeToolRegistry {

    private final Map<String, LocalLifeTool> toolMap = new HashMap<>();

    public LocalLifeToolRegistry(List<LocalLifeTool> tools) {
        tools.forEach(t -> toolMap.put(t.name(), t));
    }

    public List<LocalLifeTool> allTools() {
        return new ArrayList<>(toolMap.values());
    }

    public String runTool(String name, String query) {
        return toolMap.get(name).execute(query);
    }
}
