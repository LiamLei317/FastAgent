package com.fast.agent.core.langgraph.nodes;

import com.fast.agent.core.langgraph.state.SkillGraphState;
import com.fast.agent.core.skills.SkillMappingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过原子 skill 和业务 skill 映射关系，加载业务 skill
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoadBusinessNode {
    private final SkillMappingManager skillMappingManager;

    public SkillGraphState execute(SkillGraphState state) {
        log.info("LoadBusinessNode-start: {}", state);
        String atomicSkillCode = state.getAtomicSkillCode();
        List<String> businessList = skillMappingManager.getBusinessSkillCodes(atomicSkillCode);

        // ====================== 【安全兜底：必须保证有值】 ======================
        String selectedBusinessCode;
        if (businessList == null || businessList.isEmpty()) {
            // 空的话给一个默认值，保证不崩溃
            selectedBusinessCode = "DEFAULT_BUSINESS";
        } else {
            // 正常情况取第一个
            selectedBusinessCode = businessList.get(0);
        }

        // ====================== 【关键：必须创建新状态，不能修改旧的】 ======================
        Map<String, Object> newData = new HashMap<>(state.data());
        newData.put("selectedBusinessCode", selectedBusinessCode);

        SkillGraphState newState = new SkillGraphState(newData);
        log.info("LoadBusinessNode-end: {}", newState);
        return newState;
    }
}
