package com.fast.agent.core.skills;

import cn.hutool.core.io.resource.ResourceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 原子 skill -> 业务 skill 映射管理器
 */
@Component
@RequiredArgsConstructor
public class SkillMappingManager {

    private Map<String, List<String>> mapping;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        try {
            // 加载技能映射文件
            ClassPathResource resource = new ClassPathResource("skills/business/skills_mapping.json");
            mapping =  objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("初始化技能映射失败", e);
        }
    }
    public List<String> getBusinessSkillCodes(String atomicSkillCode) {
        return mapping.getOrDefault(atomicSkillCode, List.of());
    }
}
