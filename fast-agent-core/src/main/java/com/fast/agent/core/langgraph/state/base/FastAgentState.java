package com.fast.agent.core.langgraph.state.base;

import com.fast.agent.model.entity.Message;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastAgentState extends AgentState {

    // ===================== 框架要求的构造 =====================
    public FastAgentState(Map<String, Object> data) {
        super(data);
    }

    // ===================== 不可变更新 =====================
    private FastAgentState with(String key, Object value) {
        Map<String, Object> newData = new HashMap<>(this.data());
        newData.put(key, value);
        return new FastAgentState(newData);
    }

    // ===================== With 方法 =====================
    public FastAgentState withSessionId(String sessionId) {
        return with("sessionId", sessionId);
    }

    public FastAgentState withUserId(String userId) {
        return with("userId", userId);
    }

    public FastAgentState withRequestId(String requestId) {
        return with("requestId", requestId);
    }

    public FastAgentState withMessageHistory(List<Message> messageHistory) {
        return with("messageHistory", messageHistory);
    }

    public FastAgentState withUserQuery(String userQuery) {
        return with("userQuery", userQuery);
    }

    public FastAgentState withCleanedQuery(String cleanedQuery) {
        return with("cleanedQuery", cleanedQuery);
    }

    public FastAgentState withThought(String thought) {
        return with("thought", thought);
    }

    public FastAgentState withThinkSteps(List<String> thinkSteps) {
        return with("thinkSteps", thinkSteps);
    }

    public FastAgentState withIntent(String intent) {
        return with("intent", intent);
    }

    public FastAgentState withSkillCode(String skillCode) {
        return with("skillCode", skillCode);
    }

    public FastAgentState withSystemPrompt(String systemPrompt) {
        return with("systemPrompt", systemPrompt);
    }

    public FastAgentState withToolExecutionRequests(List<ToolExecutionRequest> toolExecutionRequests) {
        return with("toolExecutionRequests", toolExecutionRequests);
    }

    public FastAgentState withToolResults(List<String> toolResults) {
        return with("toolResults", toolResults);
    }

    public FastAgentState withNeedToolCall(boolean needToolCall) {
        return with("needToolCall", needToolCall);
    }

    public FastAgentState withToolAllDone(boolean toolAllDone) {
        return with("toolAllDone", toolAllDone);
    }

    public FastAgentState withFinalAnswer(String finalAnswer) {
        return with("finalAnswer", finalAnswer);
    }

    public FastAgentState withIsEnd(boolean isEnd) {
        return with("isEnd", isEnd);
    }

    public FastAgentState withExtContext(Map<String, Object> extContext) {
        return with("extContext", extContext);
    }

    // ===================== Getter =====================
    public String getSessionId() {
        return (String) data().get("sessionId");
    }

    public String getUserId() {
        return (String) data().get("userId");
    }

    public String getRequestId() {
        return (String) data().get("requestId");
    }

    public List<Message> getMessageHistory() {
        return (List<Message>) data().get("messageHistory");
    }

    public String getUserQuery() {
        return (String) data().get("userQuery");
    }

    public String getCleanedQuery() {
        return (String) data().get("cleanedQuery");
    }

    public String getThought() {
        return (String) data().get("thought");
    }

    public List<String> getThinkSteps() {
        return (List<String>) data().get("thinkSteps");
    }

    public String getIntent() {
        return (String) data().get("intent");
    }

    public String getSkillCode() {
        return (String) data().get("skillCode");
    }

    public String getSystemPrompt() {
        return (String) data().get("systemPrompt");
    }

    public List<ToolExecutionRequest> getToolExecutionRequests() {
        return (List<ToolExecutionRequest>) data().get("toolExecutionRequests");
    }

    public List<String> getToolResults() {
        return (List<String>) data().get("toolResults");
    }

    public boolean isNeedToolCall() {
        return (boolean) data().getOrDefault("needToolCall", false);
    }

    public boolean isToolAllDone() {
        return (boolean) data().getOrDefault("toolAllDone", false);
    }

    public String getFinalAnswer() {
        return (String) data().get("finalAnswer");
    }

    public boolean isEnd() {
        return (boolean) data().getOrDefault("isEnd", false);
    }

    public Map<String, Object> getExtContext() {
        return (Map<String, Object>) data().get("extContext");
    }

    // ===================== 禁用 Setter =====================
    @Deprecated public void setSessionId(String sessionId) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setUserId(String userId) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setRequestId(String requestId) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setMessageHistory(List<Message> messageHistory) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setUserQuery(String userQuery) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setCleanedQuery(String cleanedQuery) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setThought(String thought) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setThinkSteps(List<String> thinkSteps) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setIntent(String intent) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setSkillCode(String skillCode) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setSystemPrompt(String systemPrompt) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setToolExecutionRequests(List<ToolExecutionRequest> toolExecutionRequests) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setToolResults(List<String> toolResults) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setNeedToolCall(boolean needToolCall) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setToolAllDone(boolean toolAllDone) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setFinalAnswer(String finalAnswer) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setEnd(boolean end) { throw new UnsupportedOperationException("不可变"); }
    @Deprecated public void setExtContext(Map<String, Object> extContext) { throw new UnsupportedOperationException("不可变"); }

}