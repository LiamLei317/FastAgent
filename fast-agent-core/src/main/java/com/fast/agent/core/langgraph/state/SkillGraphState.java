package com.fast.agent.core.langgraph.state;

import com.fast.agent.core.langgraph.SkillGraphService;
import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;

/**
 * LangGraph 状态类（必须继承 AgentState）
 */
public class SkillGraphState extends AgentState {

    // 必须的构造函数（LangGraph4j 强制要求）
    public SkillGraphState(Map<String, Object> data) {
        super(data);
    }

    // ===================== 字段 Getter / Setter =====================
    public String getUserInput() {
        return (String) data().get("userInput");
    }

    public void setUserInput(String userInput) {
        data().put("userInput", userInput);
    }

    public String getSessionId() {
        return (String) data().get("sessionId");
    }

    public void setSessionId(String sessionId) {
        data().put("sessionId", sessionId);
    }

    public String getAtomicSkillCode() {
        return (String) data().get("atomicSkillCode");
    }

    public void setAtomicSkillCode(String atomicSkillCode) {
        data().put("atomicSkillCode", atomicSkillCode);
    }

    public String getSelectedBusinessCode() {
        return (String) data().get("selectedBusinessCode");
    }

    public void setSelectedBusinessCode(String selectedBusinessCode) {
        data().put("selectedBusinessCode", selectedBusinessCode);
    }

    public String getFinalSystemPrompt() {
        return (String) data().get("finalSystemPrompt");
    }

    public void setFinalSystemPrompt(String finalSystemPrompt) {
        data().put("finalSystemPrompt", finalSystemPrompt);
    }

    public String getAnswer() {
        return (String) data().get("answer");
    }

    public void setAnswer(String answer) {
        data().put("answer", answer);
    }

    public String getCheckResult() {
        return (String) data().get("checkResult");
    }

    public void setCheckResult(String checkResult) {
        data().put("checkResult", checkResult);
    }

    public Integer getRetryCount() {
        return (Integer) data().getOrDefault("retryCount", 0);
    }

    public void setRetryCount(Integer retryCount) {
        data().put("retryCount", retryCount);
    }
}