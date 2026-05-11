package com.fast.agent.core.langgraph.nodes;

import com.fast.agent.core.langgraph.state.SkillGraphState;
import com.fast.agent.core.llm.ChatModelFactory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 回答校验节点
 */
@Component
@RequiredArgsConstructor
public class AnswerCheckNode {

    // ====================== 【关键修复1】改用工厂，绝对不直接注入 ======================
    private final ChatModelFactory chatModelFactory;

    public SkillGraphState execute(SkillGraphState oldState) {
        String userInput = oldState.getUserInput();
        String answer = oldState.getAnswer();

        // 安全兜底
        if (answer == null) answer = "";

        String prompt = """
            你是回答校验器，判断结果是否合格，仅返回标记：
            PASS / FORMAT_ERROR / BUSINESS_MISMATCH / INTENT_ERROR
            
            用户问题：%s
            回答：%s
            """.formatted(userInput, answer);

        // ====================== 【关键修复2】从工厂获取正确模型 ======================
        ChatLanguageModel model = chatModelFactory.createChatModel();
        String result = model.generate(prompt).trim();

        // ====================== 【关键修复3】创建新状态，不修改旧状态 ======================
        Map<String, Object> newData = new HashMap<>(oldState.data());
        newData.put("checkResult", result);

        return new SkillGraphState(newData);
    }
}