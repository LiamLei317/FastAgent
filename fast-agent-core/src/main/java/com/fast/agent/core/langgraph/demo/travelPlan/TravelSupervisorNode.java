package com.fast.agent.core.langgraph.demo.travelPlan;

import com.fast.agent.core.llm.ChatModelFactory;
import lombok.AllArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;

@Component
@AllArgsConstructor
public class TravelSupervisorNode implements AsyncNodeAction<TravelState> {

    private final ChatModelFactory chatModelFactory;

    @Override
    public CompletableFuture<Map<String, Object>> apply(TravelState state) {

        Map<String, Object> returnMap = Map.of();
        String userDemand = state.userDemand().orElse("");

        // 第一步：还没有执行计划 → 先让大模型生成执行计划
        if (state.executePlan().isEmpty()) {
            String planPrompt = """
                    你是旅行规划总负责人，请根据用户出行需求，制定一份简短执行计划。
                    本次任务分为三个环节：景点推荐、美食推荐、住宿推荐，按顺序执行即可。
                    只需输出一句话执行方案，不要多余内容。
                    用户需求：%s
                    """.formatted(userDemand);
            String plan = chatModelFactory.createChatModel().generate(planPrompt);

            // 生成计划后，第一个目标：景点Agent
            returnMap = Map.of(
                    "executePlan", plan,
                    "handoffTarget", "spot_agent"
            );
        }
        // 第二步：按计划串行调度 Worker
        else if (state.spotResult().isEmpty()) {
            returnMap = Map.of("handoffTarget", "spot_agent");
        } else if (state.foodResult().isEmpty()) {
            returnMap = Map.of("handoffTarget", "food_agent");
        } else if (state.hotelResult().isEmpty()) {
            returnMap = Map.of("handoffTarget", "hotel_agent");
        }
        // 第三步：所有环节完成 → 汇总生成最终完整攻略
        else if (state.finalTravelGuide().isEmpty()) {
            String spot = state.spotResult().get();
            String food = state.foodResult().get();
            String hotel = state.hotelResult().get();
            String plan = state.executePlan().get();

            String summaryPrompt = """
                    你是旅行文案整理师，结合【执行计划】、【景点】、【美食】、【住宿】内容，
                    整合为一份排版工整、阅读舒适的完整旅行攻略。
                    执行计划：%s
                    景点推荐：%s
                    美食推荐：%s
                    住宿推荐：%s
                    """.formatted(plan, spot, food, hotel);

            String finalGuide = chatModelFactory.createChatModel().generate(summaryPrompt);
            returnMap = Map.of(
                    "finalTravelGuide", finalGuide,
                    "handoffTarget", END
            );
        }
        // 全部完成，结束流程
        else {
            returnMap = Map.of("handoffTarget", END);
        }

        return CompletableFuture.completedFuture(returnMap);
    }
}
