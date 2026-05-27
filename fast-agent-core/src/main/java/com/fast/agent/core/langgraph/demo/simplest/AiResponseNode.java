package com.fast.agent.core.langgraph.demo.simplest;

import com.fast.agent.core.llm.ChatModelFactory;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class AiResponseNode implements AsyncNodeAction<ChatState> {

    private final ChatModelFactory chatModelFactory;
    @Override
    public CompletableFuture<Map<String, Object>> apply(ChatState state){
        List<String> messages = state.messages();
        String lastMsg = messages.get(messages.size() - 1);
        String reply = chatModelFactory.createChatModel().generate(lastMsg);
        return CompletableFuture.completedFuture(
                Map.of(ChatState.MESSAGES, List.of("AI：" + reply))
        );
    }
}