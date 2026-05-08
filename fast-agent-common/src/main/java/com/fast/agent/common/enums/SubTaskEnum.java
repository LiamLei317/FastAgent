package com.fast.agent.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用子任务枚举
 * 对应15个原子Skill，做一一映射
 */
@Getter
public enum SubTaskEnum {

    // 理解类
    INTENT_RECOGNIZE("意图解析", "INTENT_RECOGNIZE"),
    TASK_DECOMPOSE("任务拆解", "TASK_DECOMPOSE"),

    // 调研信息类
    MARKET_RESEARCH("市场调研", "MARKET_RESEARCH"),
    INFORMATION_SYNTHESIS("信息整合", "INFORMATION_SYNTHESIS"),
    DATA_ANALYSIS("数据分析", "DATA_ANALYSIS"),

    // 创意产品类
    CREATIVE_DESIGN("创意策划", "CREATIVE_DESIGN"),
    UX_OPTIMIZE("体验优化", "UX_OPTIMIZE"),

    // 商务成本类
    COST_QUOTE("成本报价", "COST_QUOTE"),
    NEGOTIATION_COPY("商务文案", "NEGOTIATION_COPY"),

    // 执行规划类
    RISK_ASSESSMENT("风险评估", "RISK_ASSESSMENT"),
    PRODUCTION_PLAN("生产落地规划", "PRODUCTION_PLAN"),
    TIME_SCHEDULING("时间排期", "TIME_SCHEDULING"),

    // 输出质控类
    SELF_CHECK("结果自检", "SELF_CHECK"),
    DOCUMENT_OUTPUT("文档整理输出", "DOCUMENT_OUTPUT"),
    CONVERSATION_MANAGE("多轮对话管理", "CONVERSATION_MANAGE");

    private final String taskName;
    private final String skillCode;

    SubTaskEnum(String taskName, String skillCode) {
        this.taskName = taskName;
        this.skillCode = skillCode;
    }

    private static final Map<String, String> TASK_SKILL_CACHE = new HashMap<>();

    static {
        for (SubTaskEnum e : SubTaskEnum.values()) {
            TASK_SKILL_CACHE.put(e.getTaskName(), e.getSkillCode());
        }
    }

    // ==================== 优化后：O(1) 直接获取，无循环 ====================
    public static String getSkillCodeByTaskName(String taskName) {
        return TASK_SKILL_CACHE.getOrDefault(taskName, "CONVERSATION_MANAGE");
    }
}