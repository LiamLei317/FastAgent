package com.fast.agent.core.langgraph.nodes;

import com.fast.agent.core.langgraph.state.SkillGraphState;
import com.fast.agent.core.llm.ChatModelFactory;
import com.fast.agent.core.skills.BusinessSkillManager;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择业务技能节点
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SelectBusinessNode {

    // 【修复1】改用工厂获取正确模型
    private final ChatModelFactory chatModelFactory;
    private final BusinessSkillManager businessSkillManager;

    public SkillGraphState execute(SkillGraphState oldState) {
        log.info("SelectBusinessNode-start: {}", oldState);
        String atomicSkillCode = oldState.getAtomicSkillCode();
        String userInput = oldState.getUserInput();

        List<String> atomicCodes = (atomicSkillCode == null)
                ? Collections.emptyList()
                : List.of(atomicSkillCode);

        var metaList = businessSkillManager.getMetaList(atomicCodes);
        if (metaList == null) {
            metaList = Collections.emptyList();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("从以下业务技能选择最匹配的一个，仅返回编码：\n");
        metaList.forEach(m -> {
            if (m != null) {
                sb.append(m.getCode()).append(" | ").append(m.getScene()).append("\n");
            }
        });
        sb.append("\n用户问题：").append(userInput);

        ChatLanguageModel model = chatModelFactory.createChatModel();
        String code = model.generate(sb.toString()).trim();

        Map<String, Object> newData = new HashMap<>(oldState.data());
        newData.put("selectedBusinessCode", code);

        SkillGraphState newState = new SkillGraphState(newData);
        log.info("SelectBusinessNode-end: {}", newState);
        return newState;
    }
}