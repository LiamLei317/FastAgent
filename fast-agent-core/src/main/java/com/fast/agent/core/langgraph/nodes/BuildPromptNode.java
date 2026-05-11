package com.fast.agent.core.langgraph.nodes;

import com.fast.agent.common.utils.SystemPromptUtil;
import com.fast.agent.core.langgraph.state.SkillGraphState;
import com.fast.agent.core.skills.AtomicSkillManager;
import com.fast.agent.core.skills.BusinessSkillManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 构建最终 Prompt
 * 拼接结构：
 * 1. 全局系统规则
 * 2. 原子技能定义
 * 3. 业务技能专业提示词
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BuildPromptNode {

    private final AtomicSkillManager atomicSkillManager;
    private final BusinessSkillManager businessSkillManager;

    public SkillGraphState execute(SkillGraphState oldState) {
        log.info("BuildPromptNode-start: {}", oldState);
        // 安全获取值
        String atomicSkillCode = oldState.getAtomicSkillCode();
        String businessCode = oldState.getSelectedBusinessCode();

        // ====================== 【空安全兜底，绝对不崩溃】 ======================
        String globalPrompt = SystemPromptUtil.get();
        if (globalPrompt == null) globalPrompt = "";

        String atomicPrompt = atomicSkillManager.getTemplate(atomicSkillCode);
        if (atomicPrompt == null) atomicPrompt = "";

        String businessPrompt = businessSkillManager.getPromptTemplate(businessCode);
        if (businessPrompt == null) businessPrompt = "你是一个专业的智能助手。";

        // 拼接最终 Prompt
        String finalSystemPrompt = String.join("\n\n",
                "【全局规则】", globalPrompt,
                "【原子技能】", atomicPrompt,
                "【业务技能】", businessPrompt
        );

        // ====================== 【关键：创建新状态，不修改旧状态】 ======================
        Map<String, Object> newData = new HashMap<>(oldState.data());
        newData.put("finalSystemPrompt", finalSystemPrompt);

        SkillGraphState newState = new SkillGraphState(newData);
        log.info("BuildPromptNode-end: {}", newState);
        return newState;
    }
}