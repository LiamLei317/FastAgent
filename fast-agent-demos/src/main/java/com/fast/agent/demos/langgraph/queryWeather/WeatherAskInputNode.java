package com.fast.agent.demos.langgraph.queryWeather;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class WeatherAskInputNode implements AsyncNodeAction<WeatherAskState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(WeatherAskState state) {
        // 从外部传入的参数获取用户问题
        Optional<Object> userInput = state.value("userInput");
        log.info("用户输入:{}", userInput.orElse("空"));

        // 把用户消息存入 state
        return CompletableFuture.completedFuture(Map.of(
                WeatherAskState.MESSAGES, List.of("用户：" + userInput)
        ));
    }
}
