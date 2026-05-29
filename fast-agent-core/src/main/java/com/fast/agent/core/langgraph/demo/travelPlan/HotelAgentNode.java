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
public class HotelAgentNode implements AsyncNodeAction<TravelState> {

    private final ChatModelFactory chatModelFactory;

    @Override
    public CompletableFuture<Map<String, Object>> apply(TravelState state) {
        String demand = state.userDemand().orElse("");

        // 专业住宿推荐提示词
        String prompt = """
            你是一位高性价比住宿规划师，根据用户旅行地点与需求，推荐合适的住宿。
            要求：
            1. 推荐类型：民宿 / 酒店 / 青旅
            2. 推荐位置优势
            3. 价格区间
            4. 适合人群
            
            用户需求：%s
            请直接输出推荐结果，简洁清晰。
            """.formatted(demand);

        String result = chatModelFactory.createChatModel().generate(prompt);

        Map<String, Object> update = new HashMap<>(state.data());
        update.put("hotelResult", result);
        update.remove("handoffTarget"); // 交给 supervisor 重新决策

        return CompletableFuture.completedFuture(update);
    }
}
