package com.fast.agent.core.langgraph.demo.travelPlan;


import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.action.AsyncNodeActionWithConfig;
import org.bsc.langgraph4j.hook.NodeHook;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Component
@Slf4j
public class TraceNodeHook implements NodeHook.WrapCall<TravelState> {

    /**
     * idea 反编译的问题，参数中的第一个 s 就是 nodeId
     */
    @Override
    public CompletableFuture<Map<String, Object>> applyWrap(String s, TravelState state, RunnableConfig runnableConfig, AsyncNodeActionWithConfig<TravelState> asyncNodeActionWithConfig) {
        log.info("当前执行节点:{}", s);
        return asyncNodeActionWithConfig.apply(state, runnableConfig)
                .thenApply(result -> {
                    Map<String, Object> finalResult = new HashMap<>(result);
                    state.addTrace(s);
                    finalResult.put("executionTrace", state.executionTrace());
                    return finalResult;
                });
    }
}
