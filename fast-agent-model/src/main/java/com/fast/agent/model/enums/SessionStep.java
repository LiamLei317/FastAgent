package com.fast.agent.model.enums;

import lombok.Getter;

/**
 * 会话流程步骤枚举
 * 用于管理产品创意全流程编排的4个固定阶段
 * 仅做状态引导，不强制限制用户对话
 */
@Getter
public enum SessionStep {
    
    /**
     * 创意澄清阶段 - 第一步
     */
    CREATIVE_CLARIFY("creative-clarification", 1),
    
    /**
     * 市场验证阶段 - 第二步
     */
    MARKET_ANALYSIS("market-analysis", 2),
    
    /**
     * 产品定义阶段 - 第三步
     */
    PRODUCT_DEFINITION("product-definition", 3),
    
    /**
     * 设计选择阶段 - 第四步
     */
    DESIGN_SELECTION("creative-design", 4),
    
    /**
     * 流程结束状态 - 完成态
     */
    FLOW_FINISHED("flow-finished", 5);
    
    private final String code;
    private final Integer order;
    
    SessionStep(String code, Integer order) {
        this.code = code;
        this.order = order;
    }
    
    /**
     * 获取步骤编码
     * @return 步骤编码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取步骤顺序
     * @return 步骤顺序
     */
    public Integer getOrder() {
        return order;
    }
    
    /**
     * 根据编码获取步骤类型
     * @param code 步骤编码
     * @return 步骤类型，未找到时返回 CREATIVE_CLARIFY
     */
    public static SessionStep fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return CREATIVE_CLARIFY;
        }
        
        for (SessionStep step : values()) {
            if (step.getCode().equalsIgnoreCase(code.trim())) {
                return step;
            }
        }
        
        return CREATIVE_CLARIFY;
    }
    
    /**
     * 获取下一个步骤
     * @return 下一个步骤，当前是最后一步时返回 FLOW_FINISHED
     */
    public SessionStep getNextStep() {
        return switch (this) {
            case CREATIVE_CLARIFY -> MARKET_ANALYSIS;
            case MARKET_ANALYSIS -> PRODUCT_DEFINITION;
            case PRODUCT_DEFINITION -> DESIGN_SELECTION;
            case DESIGN_SELECTION -> FLOW_FINISHED;
            case FLOW_FINISHED -> FLOW_FINISHED; // 已完成，不再推进
        };
    }
    
    /**
     * 判断是否为流程结束状态
     * @return 是否结束
     */
    public boolean isFinished() {
        return this == FLOW_FINISHED;
    }
    
    /**
     * 判断是否为有效的工作步骤（非结束态）
     * @return 是否为有效步骤
     */
    public boolean isValidStep() {
        return this != FLOW_FINISHED;
    }
    
    /**
     * 判断是否可以推进到下一个步骤
     * @return 是否可以推进
     */
    public boolean canProceed() {
        return this.isValidStep();
    }
    
    /**
     * 获取步骤的友好提示语
     * @return 步骤提示语
     */
    public String getFriendlyHint() {
        return switch (this) {
            case CREATIVE_CLARIFY -> "当前处于创意澄清阶段，请描述您的产品创意想法";
            case MARKET_ANALYSIS -> "当前处于市场验证阶段，可以分析目标市场和用户需求";
            case PRODUCT_DEFINITION -> "当前处于产品定义阶段，可以明确产品功能和规格";
            case DESIGN_SELECTION -> "当前处于设计选择阶段，可以讨论产品设计方案";
            case FLOW_FINISHED -> "产品创意全流程已完成，您可以继续讨论任何相关问题";
        };
    }
}
