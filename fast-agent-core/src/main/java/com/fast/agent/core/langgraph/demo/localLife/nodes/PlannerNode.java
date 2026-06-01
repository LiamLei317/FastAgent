package com.fast.agent.core.langgraph.demo.localLife.nodes;

import com.fast.agent.core.langgraph.demo.localLife.LocalLifeState;
import com.fast.agent.core.llm.ChatModelFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlannerNode implements DynamicNode {

    private final ChatModelFactory llm;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public String name() {
        return "planner";
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(LocalLifeState state) {
        String userInput = state.userQuery().orElse("");

        String prompt = """
            你是任务规划器。
            
            用户需求：%s
            
            可用工具：FoodTool、MallTool、ParkTool
            
            请生成执行计划，返回JSON格式：
            {
              "plan": "描述整体计划",
              "steps": ["步骤1","步骤2"] 
            }
            
            只返回JSON，不要其他内容。
            """.formatted(userInput);

        try {
            String json = llm.createChatModel().generate(prompt);
            String cleanJson = cleanResponseJson(json);
            Map<String, Object> plan = om.readValue(cleanJson, new TypeReference<Map<String, Object>>() {});
            log.info("Plan: {}", plan);
            return CompletableFuture.completedFuture(Map.of(
                    LocalLifeState.PLAN_JSON, json,
                    LocalLifeState.STEPS, plan.get("steps")
            ));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    LocalLifeState.PLAN_JSON, "{}",
                    LocalLifeState.STEPS, List.of()
            ));
        }
    }

    private String cleanResponseJson(String response) {
        if (response == null) return "{}";
        // 移除 Markdown 代码块
        response = response.replace("```json", "").replace("```", "");
        // 去除前后空白
        return response.trim();
    }
}