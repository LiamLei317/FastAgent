package com.fast.agent.demos.langgraph.queryWeather;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class WeatherToolNode implements AsyncNodeAction<WeatherAskState> {
    @Override
    public CompletableFuture<Map<String, Object>> apply(WeatherAskState state) {
        String userInput = state.value("userInput").orElse("").toString();

        // 模拟调用天气接口
        String weather = "【天气工具返回】北京当前天气：晴，25℃，微风";

        // 把工具结果加入消息
        return CompletableFuture.completedFuture(Map.of(
                WeatherAskState.MESSAGES, List.of(weather)
        ));
    }

}
