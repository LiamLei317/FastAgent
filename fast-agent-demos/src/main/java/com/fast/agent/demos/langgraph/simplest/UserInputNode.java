package com.fast.agent.demos.langgraph.simplest;


import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class UserInputNode implements AsyncNodeAction<ChatState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(ChatState state) {
        Optional<Object> userInput = state.value("userInput");
        log.info("用户输入:{}", userInput.orElse("空"));

        return CompletableFuture.completedFuture(
                Map.of(ChatState.MESSAGES, List.of("用户：" + userInput.orElse("")))
        );
    }
}
