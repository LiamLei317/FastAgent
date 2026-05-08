package com.fast.agent.common.utils;

import com.fast.agent.common.entity.Skill;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SkillsManager {

    public static final Map<String, Skill> SKILL_CACHE = new HashMap<>();
    private final ObjectMapper objectMapper;

    public SkillsManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @PostConstruct
    public void init() {
        try {
            // 加载 index.json
            ClassPathResource indexResource = new ClassPathResource("skills/index.json");
            String indexJson = indexResource.getContentAsString(StandardCharsets.UTF_8);
            List<String> fileNames = objectMapper.readValue(indexJson, new TypeReference<List<String>>() {});

            // 加载所有 skill json
            for (String fileName : fileNames) {
                ClassPathResource skillResource = new ClassPathResource("skills/" + fileName);
                String skillJson = skillResource.getContentAsString(StandardCharsets.UTF_8);
                Skill skill = objectMapper.readValue(skillJson, Skill.class);
                SKILL_CACHE.put(skill.getSkillCode(), skill);
            }

            System.out.println("本地 Skill 加载完成：" + SKILL_CACHE.size() + " 个");

        } catch (Exception e) {
            System.err.println("Skill 加载失败");
        }
    }

    // 根据skillCode获取技能
    public static Skill getSkill(String skillCode) {
        return SKILL_CACHE.get(skillCode);
    }
}
