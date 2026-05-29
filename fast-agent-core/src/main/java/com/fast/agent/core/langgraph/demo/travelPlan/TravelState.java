package com.fast.agent.core.langgraph.demo.travelPlan;

import org.bsc.langgraph4j.state.AgentState;

import java.util.*;

public class TravelState extends AgentState {

    public TravelState(Map<String, Object> data) {
        super(data);
    }

    // 用户原始出行需求
    public Optional<String> userDemand() {
        return value("userDemand");
    }

    // 景点推荐结果
    public Optional<String> spotResult() {
        return value("spotResult");
    }

    // 美食推荐结果
    public Optional<String> foodResult() {
        return value("foodResult");
    }

    // 住宿推荐结果
    public Optional<String> hotelResult() {
        return value("hotelResult");
    }

    // 执行计划（Supervisor 生成）
    public Optional<String> executePlan() {
        return value("executePlan");
    }

    // 最终整合后的完整攻略
    public Optional<String> finalTravelGuide() {
        return value("finalTravelGuide");
    }

    // 核心：交接目标节点（用于 Handoff）
    public Optional<String> handoffTarget() {
        return value("handoffTarget");
    }

    @SuppressWarnings("unchecked")
    public List<String> executionTrace() {
        return value("executionTrace")
                .map(list -> (List<String>) list)
                .orElse(new ArrayList<>());
    }

    public TravelState addTrace(String nodeId) {
        // 拿到所有的 trace
        List<String> trace = executionTrace();
        // 添加新 trace
        trace.add(nodeId);
        // 更新 data
        HashMap<String, Object> newTrace = new HashMap<>(data());
        newTrace.put("executionTrace", trace);
        return new TravelState(newTrace);
    }
}
