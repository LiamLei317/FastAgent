package com.fast.agent.core.langgraph.demo.travelPlan;

import com.fast.agent.core.llm.ChatModelFactory;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class FoodAgentNode implements AsyncNodeAction<TravelState> {

    private final ChatModelFactory chatModelFactory;

    @Override
    public CompletableFuture<Map<String, Object>> apply(TravelState state) {
        String demand = state.userDemand().orElse("");

        // 专业美食推荐提示词
        String prompt = """
            你是一位当地资深美食探店博主，根据用户的旅行地点，推荐3种必吃特色美食。
            要求：
            1. 说明美食特色
            2. 推荐口味
            3. 预估人均消费
            
            用户需求：%s
            请直接输出推荐结果，简洁实用。
            """.formatted(demand);

        String result = chatModelFactory.createChatModel().generate(prompt);

        Map<String, Object> update = new HashMap<>(state.data());
        update.put("foodResult", result);
        update.remove("handoffTarget"); // 交给 supervisor 重新决策

        return CompletableFuture.completedFuture(update);
    }
}