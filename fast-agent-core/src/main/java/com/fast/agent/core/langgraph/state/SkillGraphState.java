package com.fast.agent.core.langgraph.state;
import org.bsc.langgraph4j.state.AgentState;
import java.util.HashMap;
import java.util.Map;

/**
 * LangGraph 状态类（不可变、安全、无硬编码、IDE提示）
 */
public class SkillGraphState extends AgentState {

    // 必须构造函数
    public SkillGraphState(Map<String, Object> data) {
        super(data);
    }

    // =========================================
    // 【核心工具】不可变更新：返回新State
    // =========================================
    private SkillGraphState with(String key, Object value) {
        Map<String, Object> newData = new HashMap<>(this.data());
        newData.put(key, value);
        return new SkillGraphState(newData);
    }

    // =========================================
    // 不可变更新方法（无硬编码、安全、可重构）
    // =========================================
    public SkillGraphState withUserInput(String userInput) {
        return with("userInput", userInput);
    }

    public SkillGraphState withSessionId(String sessionId) {
        return with("sessionId", sessionId);
    }

    public SkillGraphState withAtomicSkillCode(String atomicSkillCode) {
        return with("atomicSkillCode", atomicSkillCode);
    }

    public SkillGraphState withSelectedBusinessCode(String selectedBusinessCode) {
        return with("selectedBusinessCode", selectedBusinessCode);
    }

    public SkillGraphState withFinalSystemPrompt(String finalSystemPrompt) {
        return with("finalSystemPrompt", finalSystemPrompt);
    }

    public SkillGraphState withAnswer(String answer) {
        return with("answer", answer);
    }

    public SkillGraphState withCheckResult(String checkResult) {
        return with("checkResult", checkResult);
    }

    public SkillGraphState withRetryCount(Integer retryCount) {
        return with("retryCount", retryCount);
    }

    // =========================================
    // Getter（保持不变）
    // =========================================
    public String getUserInput() {
        return (String) data().get("userInput");
    }

    public String getSessionId() {
        return (String) data().get("sessionId");
    }

    public String getAtomicSkillCode() {
        return (String) data().get("atomicSkillCode");
    }

    public String getSelectedBusinessCode() {
        return (String) data().get("selectedBusinessCode");
    }

    public String getFinalSystemPrompt() {
        return (String) data().get("finalSystemPrompt");
    }

    public String getAnswer() {
        return (String) data().get("answer");
    }

    public String getCheckResult() {
        return (String) data().get("checkResult");
    }

    public Integer getRetryCount() {
        return (Integer) data().getOrDefault("retryCount", 0);
    }

    // =========================================
    // 禁用危险的 Setter（LangGraph 不应该用）
    // =========================================
    @Deprecated
    public void setUserInput(String userInput) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
    @Deprecated
    public void setSessionId(String sessionId) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
    @Deprecated
    public void setAtomicSkillCode(String atomicSkillCode) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
    @Deprecated
    public void setSelectedBusinessCode(String selectedBusinessCode) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
    @Deprecated
    public void setFinalSystemPrompt(String finalSystemPrompt) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
    @Deprecated
    public void setAnswer(String answer) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
    @Deprecated
    public void setCheckResult(String checkResult) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
    @Deprecated
    public void setRetryCount(Integer retryCount) { throw new UnsupportedOperationException("不可变状态禁止修改！"); }
}