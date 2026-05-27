package com.fast.agent.core.langgraph.demo.queryWeather;

import com.fast.agent.core.llm.ChatModelFactory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class AiJudgeNode implements AsyncNodeAction<WeatherAskState> {
    private final ChatModelFactory chatModelFactory;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WeatherAskState state) {
        ChatLanguageModel chatModel = chatModelFactory.createChatModel();

        // 获取用户问题
        String userInput = state.value("userInput").orElse("").toString();

        String prompt = """
                判断用户问题是否需要查询天气：
                问题：%s
                
                只返回【需要】或【不需要】
                """.formatted(userInput);

        String answer = chatModel.generate(prompt);

        // 返回判断结果（存入state）
        return CompletableFuture.completedFuture(Map.of(
                "needWeather", answer.contains("需要")
        ));
    }

}
