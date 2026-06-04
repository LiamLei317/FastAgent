package com.fast.agent.demos.langgraph.localLife.nodes;


import com.fast.agent.demos.langgraph.localLife.LocalLifeState;
import com.fast.agent.demos.langgraph.localLife.tools.LocalLifeTool;
import com.fast.agent.demos.langgraph.localLife.tools.LocalLifeToolRegistry;
import com.fast.agent.core.llm.ChatModelFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionNode implements DynamicNode {

    private final ChatModelFactory llm;
    private final LocalLifeToolRegistry localLifeToolRegistry;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public String name() {
        return "decision";
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(LocalLifeState state) {
        String userQuery = state.userQuery().orElse("");
        List<String> history = state.chatHistory();
        List<LocalLifeTool> tools = localLifeToolRegistry.allTools();

        // 提示词：让 LLM 返回 nextNode + toolName
        String prompt = """
    你是一个路由控制器，只能从下面选项中【选一个】作为 nextNode：
    
    规则：
    1. 如果需要查信息 → nextNode = "tool"
    2. 如果可以直接回答 → nextNode = "answer"
    3. 如果结束对话 → nextNode = "end"
    
    绝对不允许多选！
    绝对不允许写 tool/MallTool 这种组合值！
    只允许三选一："tool" "answer" "end"
    
    历史对话：%s
    用户问题：%s
    
    工具列表：%s
    
    只返回纯净JSON，不要任何解释：
    {"nextNode":"","toolName":"","content":""}
    """.formatted(history, userQuery, tools);

        try {
            String json = llm.createChatModel().generate(prompt);
            String cleanJson = cleanResponseJson(json);
            Map<String, Object> llmResult = om.readValue(cleanJson, new TypeReference<Map<String, Object>>() {});
            log.info("LLM Result: {}", llmResult);

            String nextNode = (String) llmResult.get("nextNode");
            String toolName = (String) llmResult.get("toolName");
            String content = (String) llmResult.get("content");

            Map<String, Object> output = new HashMap<>();

            if ("tool".equals(nextNode)) {
                // 工具调用：把要调用的工具名存在 steps 里，让路由识别
                output.put(LocalLifeState.STEPS, List.of(toolName));
            } else if ("answer".equals(nextNode)) {
                output.put(LocalLifeState.FINAL_ANSWER, content);
            }

            return CompletableFuture.completedFuture(output);

        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    LocalLifeState.FINAL_ANSWER, "我暂时无法处理你的请求"
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