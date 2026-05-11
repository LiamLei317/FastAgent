package com.fast.agent.core.skills;

import com.fast.agent.common.entity.AtomicSkill;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 原子技能管理器（适配你 index.json 控制启用列表的结构）
 */
@Component
@RequiredArgsConstructor
public class AtomicSkillManager {

    private final ObjectMapper objectMapper;

    private final Map<String, AtomicSkill> skillMap = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        // 读取 index.json，获取当前启用的原子技能列表
        ClassPathResource indexResource = new ClassPathResource("skills/atomic/index.json");
        List<String> skillFileNames = objectMapper.readValue(
                indexResource.getInputStream(),
                new TypeReference<>() {}
        );

        for (String fileName : skillFileNames) {
            String filePath = "skills/atomic/" + fileName;
            ClassPathResource skillResource = new ClassPathResource(filePath);

            // 只加载 JSON，不读取 MD
            AtomicSkill skill = objectMapper.readValue(skillResource.getInputStream(), AtomicSkill.class);
            skillMap.put(skill.getCode(), skill);
        }
    }

    /**
     * 根据 code 获取原子技能的完整 Prompt
     */
    public String getTemplate(String code) {
        AtomicSkill skill = skillMap.get(code);
        return skill == null ? "" : skill.getPromptTemplate();
    }

    /**
     * 根据 code 获取完整的原子技能对象
     */
    public AtomicSkill getSkill(String code) {
        return skillMap.get(code);
    }

    /**
     * 获取所有已启用的原子技能列表
     */
    public List<AtomicSkill> listAllEnabled() {
        return new ArrayList<>(skillMap.values());
    }
}