package com.fast.agent.demos.langgraph.travelPlan;

import com.fast.agent.core.llm.ChatModelFactory;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.LG4JLoggable.log;
import static org.bsc.langgraph4j.StateGraph.END;

@Component
@AllArgsConstructor
public class TravelSupervisorNode implements AsyncNodeAction<TravelState> {

    private final ChatModelFactory chatModelFactory;

    @Override
    public CompletableFuture<Map<String, Object>> apply(TravelState state) {

        Map<String, Object> returnMap = new HashMap<>(state.data());

        // 第一次必须生成执行计划
        if (state.executePlan().isEmpty()) {
            String planPrompt = """
                    你是旅行规划总负责人，请根据用户出行需求，制定一份简短执行计划。
                    本次任务分为三个环节：景点推荐、美食推荐、住宿推荐，按顺序执行。
                    只需输出一句话执行方案，不要多余内容。
                    用户需求：%s
                    """.formatted(state.userDemand().orElse(""));

            String plan = chatModelFactory.createChatModel().generate(planPrompt);
            returnMap.put("executePlan", plan);
            returnMap.put("handoffTarget", "spot_agent");
            return CompletableFuture.completedFuture(returnMap);
        }

        String reactPrompt = """
            你是旅行规划智能体，严格按照完成情况判断下一步。
            
            已完成状态：
            执行计划：%s
            景点结果：%s
            美食结果：%s
            住宿结果：%s

            规则（必须严格遵守）：
            1. 如果 景点、美食、住宿 全部都有结果 → 输出 FINISH
            2. 如果有任何一个没有结果 → 输出对应的 agent：spot_agent、food_agent、hotel_agent
            3. 只能输出一个值
            4. 不要输出任何多余内容
            
            输出：
            """.formatted(
                state.executePlan().orElse("未生成"),
                state.spotResult().isPresent() ? "已完成" : "未完成",
                state.foodResult().isPresent() ? "已完成" : "未完成",
                state.hotelResult().isPresent() ? "已完成" : "未完成"
        );

        String decision = chatModelFactory.createChatModel().generate(reactPrompt).trim();
        log.info("ReAct 决策：{}", decision);

        // ===================== 路由 =====================
        if ("FINISH".equals(decision)) {
            returnMap.put("handoffTarget", END);
            returnMap.put("finalTravelGuide", getFinalTravelGuide(state));
        } else {
            returnMap.put("handoffTarget", decision);
        }

        return CompletableFuture.completedFuture(returnMap);
    }

    private String getFinalTravelGuide(TravelState state) {
        log.info("生成执行计划：{}", state.executePlan());
        String summaryPrompt = """
            你是专业旅行攻略师，将以下内容整合成一篇完整、美观、易读的旅行攻略：
            
            执行计划：%s
            景点推荐：%s
            美食推荐：%s
            住宿推荐：%s
            
            要求：排版舒适、内容完整、语言流畅。
            """.formatted(
                state.executePlan().orElse(""),
                state.spotResult().orElse(""),
                state.foodResult().orElse(""),
                state.hotelResult().orElse(""));
        String finalTravelGuide = chatModelFactory.createChatModel().generate(summaryPrompt);
        log.info("生成的旅行指南：{}", finalTravelGuide);
        return finalTravelGuide;
    }
}