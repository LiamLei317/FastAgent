package com.fast.agent.core.langgraph.demo.travelPlan;

import com.fast.agent.core.llm.ChatModelFactory;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class SpotAgentNode implements AsyncNodeAction<TravelState> {

    private final ChatModelFactory chatModelFactory;

    @Override
    public CompletableFuture<Map<String, Object>> apply(TravelState state) {
        String demand = state.userDemand().orElse("");
        // 专业景点提示词
        String prompt = """
            你是一位专业的旅行景点规划师，根据用户的出行需求，推荐当地最值得去的3个景点。
            要求：
            1. 每个景点说明亮点
            2. 建议游玩时长
            3. 语言简洁、实用、接地气
            
            用户需求：%s
            请直接输出景点推荐结果，不要多余开场白。
            """.formatted(demand);

        String result = chatModelFactory.createChatModel().generate(prompt);

        return CompletableFuture.completedFuture(Map.of(
                "spotResult", result,
                "handoffTarget", "supervisor"
        ));
    }
}
