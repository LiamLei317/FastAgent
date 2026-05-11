package com.fast.agent.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class BaseSkill {
    private String code;
    private String name;
    private String scene;
    private List<String> keywords;
    private String promptTemplate;
}
