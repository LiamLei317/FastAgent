package com.fast.agent.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class Skill {
    private String skillCode;
    private String skillName;
    private String desc;
    private List<String> tags;
    private String promptTemplate;
}
