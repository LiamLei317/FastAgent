package com.fast.agent.core.langgraph.nodes;

import com.fast.agent.core.langgraph.state.SkillGraphState;
import com.fast.agent.core.llm.ChatModelFactory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 生成回答
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateAnswerNode {

    private final ChatModelFactory chatModelFactory;

    public SkillGraphState execute(SkillGraphState state) {
        log.info("GenerateAnswerNode-start: {}", state);
        // 拼接提示词
        String fullPrompt = state.getFinalSystemPrompt() + "\n用户问题：" + state.getUserInput();
        ChatLanguageModel model = chatModelFactory.createChatModel();
        String answer = model.generate(fullPrompt);
        // 新状态
        Map<String, Object> newData = new HashMap<>(state.data());
        newData.put("answer", answer);

        SkillGraphState newState = new SkillGraphState(newData);
        log.info("GenerateAnswerNode-end: {}", newState);
        return newState;
    }
}