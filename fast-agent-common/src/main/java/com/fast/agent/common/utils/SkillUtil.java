package com.fast.agent.common.utils;

import com.fast.agent.common.entity.Skill;

public class SkillUtil {

    public static String buildFullSkill(String skillCode, String userInput) {
        Skill skill = SkillsManager.getSkill(skillCode);
        if (skill == null) {
            return SystemPromptUtil.get() + "\n用户需求：" + userInput;
        }

        String skillPrompt = skill.getPromptTemplate()
                .replace("{userInput}", userInput);

        return SystemPromptUtil.get() + "\n" + skillPrompt;
    }

    public static String buildFullSkill(String skillCode, String userInput, String ragKnowledge) {
        String prompt = buildFullSkill(skillCode, userInput);
        if (ragKnowledge != null) {
            prompt = prompt.replace("{ragKnowledge}", ragKnowledge);
        }
        return prompt;
    }
}
