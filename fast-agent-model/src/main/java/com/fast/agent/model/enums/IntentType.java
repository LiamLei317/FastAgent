package com.fast.agent.model.enums;

/**
 * 意图类型枚举
 * 用于标识用户问题的业务意图，对应 skills 目录下的 md 文件
 */
public enum IntentType {
    
    /**
     * 默认意图，对应 default.md
     */
    DEFAULT("default", "default.md"),
    
    /**
     * 创意澄清意图，对应 creative-clarification-skill.md
     */
    CREATIVE_CLARIFICATION("creative-clarification", "creative-clarification-skill.md"),
    
    /**
     * 市场验证意图，对应 market-analysis-skill.md
     */
    MARKET_ANALYSIS("market-analysis", "market-analysis-skill.md"),
    
    /**
     * 产品定义意图，对应 product-definition-skill.md
     */
    PRODUCT_DEFINITION("product-definition", "product-definition-skill.md"),
    
    /**
     * 设计选择意图，对应 creative-design-skill.md
     */
    CREATIVE_DESIGN("creative-design", "creative-design-skill.md"),
    
    /**
     * 代码审查意图，对应 code_review.md
     */
    CODE_REVIEW("code_review", "code_review.md"),
    
    /**
     * 数据分析意图，对应 data_analysis.md
     */
    DATA_ANALYSIS("data_analysis", "data_analysis.md"),
    
    /**
     * 文档生成意图，对应 doc_generation.md
     */
    DOC_GENERATION("doc_generation", "doc_generation.md");
    
    private final String code;
    private final String fileName;
    
    IntentType(String code, String fileName) {
        this.code = code;
        this.fileName = fileName;
    }
    
    /**
     * 获取意图编码
     * @return 意图编码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取对应的技能文件名
     * @return 文件名
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * 根据编码获取意图类型
     * @param code 意图编码
     * @return 意图类型，未找到时返回 DEFAULT
     */
    public static IntentType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return DEFAULT;
        }
        
        for (IntentType intent : values()) {
            if (intent.getCode().equalsIgnoreCase(code.trim())) {
                return intent;
            }
        }
        
        return DEFAULT;
    }
}
