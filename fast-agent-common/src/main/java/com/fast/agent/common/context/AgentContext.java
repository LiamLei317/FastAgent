package com.fast.agent.common.context;

import lombok.Data;

import java.util.List;

@Data
public class AgentContext {
    // 用户原始输入
    private String userInput;
    // 行业/业务RAG参考知识
    private String ragIndustryKnow;
    // 拆解后的子任务列表
    private List<String> subTaskList;
    // 每一步执行结果
    private List<String> stepResultList;
    // 全部执行汇总内容
    private String allSummaryContent;
    // 最终交付输出
    private String finalOutput;
}