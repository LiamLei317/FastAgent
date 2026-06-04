package com.fast.agent.demos.langgraph.localLife.nodes;

import com.fast.agent.demos.langgraph.localLife.LocalLifeState;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface DynamicNode extends AsyncNodeAction<LocalLifeState> {
    String name();
}
