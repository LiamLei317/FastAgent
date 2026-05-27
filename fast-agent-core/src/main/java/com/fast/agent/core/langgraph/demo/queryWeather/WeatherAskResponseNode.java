package com.fast.agent.core.langgraph.demo.queryWeather;

import com.fast.agent.core.llm.ChatModelFactory;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class WeatherAskResponseNode implements AsyncNodeAction<WeatherAskState> {

    private final ChatModelFactory chatModelFactory;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WeatherAskState state) {
        var chatModel = chatModelFactory.createChatModel();
        String userInput = state.value("userInput").orElse("").toString();
        boolean needWeather = (Boolean) state.value("needWeather").orElse(false);

        String prompt;
        if (needWeather) {
            prompt = "用户问：%s，已查询到天气，请自然回答".formatted(userInput);
        } else {
            prompt = "用户问：%s，请直接回答".formatted(userInput);
        }

        String aiMessage = "AI：" + chatModel.generate(prompt);
        return CompletableFuture.completedFuture(Map.of(
                WeatherAskState.MESSAGES, List.of(aiMessage)
        ));
    }

}
