package com.fast.agent.core.skills;

import com.fast.agent.common.entity.BusinessSkill;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 业务技能管理器（最终版）
 * 结构：
 * 1. schema/business_skills_schema.json → 元数据
 * 2. content/xxx.md → 技能内容
 */
@Component
@RequiredArgsConstructor
public class BusinessSkillManager {

    private final ObjectMapper objectMapper;

    /**
     * 缓存：code -> BusinessSkill
     */
    private final Map<String, BusinessSkill> skillMap = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        // 加载所有业务技能元数据
        String schemaPath = "skills/business/business_skills_schema.json";
        ClassPathResource schemaResource = new ClassPathResource(schemaPath);

        List<BusinessSkill> skillList = objectMapper.readValue(
                schemaResource.getInputStream(),
                new TypeReference<>() {}
        );

        // 加载每个业务对应的 MD 内容
        for (BusinessSkill skill : skillList) {
            String code = skill.getCode();
            String mdPath = "skills/business/content/" + code + ".md";
            ClassPathResource mdResource = new ClassPathResource(mdPath);

            // 读取 MD 内容
            String content = FileCopyUtils.copyToString(
                    new InputStreamReader(mdResource.getInputStream(), StandardCharsets.UTF_8)
            );

            skill.setPromptTemplate(content);
            skillMap.put(code, skill);
        }
    }

    /**
     * 根据业务编码列表，批量获取元数据
     */
    public List<BusinessSkill> getMetaList(List<String> businessCodes) {
        if (businessCodes == null || businessCodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<BusinessSkill> result = new ArrayList<>();
        for (String code : businessCodes) {
            BusinessSkill skill = skillMap.get(code);
            if (skill != null) {
                result.add(skill);
            }
        }
        return result;
    }

    /**
     * 获取完整的业务技能提示词
     */
    public String getPromptTemplate(String code) {
        BusinessSkill skill = skillMap.get(code);
        return skill == null ? "" : skill.getPromptTemplate();
    }

    /**
     * 获取完整业务技能对象
     */
    public BusinessSkill getSkill(String code) {
        return skillMap.get(code);
    }
}