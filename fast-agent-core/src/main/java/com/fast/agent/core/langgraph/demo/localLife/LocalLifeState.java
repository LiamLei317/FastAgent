package com.fast.agent.core.langgraph.demo.localLife;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.state.Reducer;

import java.util.*;

@SuppressWarnings("unchecked")
public class LocalLifeState extends AgentState {

    public static final String USER_INPUT    = "userInput";
    public static final String MEMORY        = "memory";
    public static final String PLAN_JSON     = "planJson";
    public static final String STEPS         = "steps";
    public static final String CURRENT_STEP  = "currentStep";
    public static final String TOOL_RESULTS  = "toolResults";
    public static final String FINAL_ANSWER  = "finalAnswer";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            USER_INPUT,    Channels.base(overwrite(), () -> ""),
            PLAN_JSON,     Channels.base(overwrite(), () -> ""),
            CURRENT_STEP,  Channels.base(overwrite(), () -> 0),
            FINAL_ANSWER,  Channels.base(overwrite(), () -> ""),
            MEMORY,        Channels.appender(ArrayList::new),
            STEPS,         Channels.appender(ArrayList::new),
            TOOL_RESULTS,  Channels.base(LocalLifeState::mergeMap, HashMap::new)
    );

    private static <T> Reducer<T> overwrite() {
        return (oldVal, newVal) -> newVal;
    }

    private static Map<String, String> mergeMap(Map<String, String> oldVal, Map<String, String> newVal) {
        if (oldVal == null) return newVal;
        Map<String, String> merged = new HashMap<>(oldVal);
        merged.putAll(newVal);
        return merged;
    }

    public LocalLifeState(Map<String, Object> initData) {
        super(initData);
    }

    // Getter 方法保持不变
    public Optional<String> userQuery() {
        return value(USER_INPUT);
    }

    public List<String> chatHistory() {
        return value(MEMORY).map(it -> (List<String>) it).orElse(new ArrayList<>());
    }

    public Optional<String> planJson() {
        return value(PLAN_JSON);
    }

    public List<String> steps() {
        return value(STEPS).map(it -> (List<String>) it).orElse(new ArrayList<>());
    }

    public Optional<Integer> currentStep() {
        return value(CURRENT_STEP);
    }

    public Map<String, String> toolResults() {
        return value(TOOL_RESULTS).map(it -> (Map<String, String>) it).orElse(new HashMap<>());
    }

    public Optional<String> finalAnswer() {
        return value(FINAL_ANSWER);
    }

    // 判断是否需要执行工具
    public boolean isNeedsTool() {
        return !steps().isEmpty();
    }

    // ✅ 【关键】消费步骤，返回新的不可变 State（无报错版本）
    public LocalLifeState consumeFirstStep() {
        List<String> steps = steps();
        if (steps.isEmpty()) {
            return this;
        }

        // 复制一份新的 steps，移除第一个元素
        List<String> newSteps = new ArrayList<>(steps);
        newSteps.remove(0);

        // 复制当前 state 的所有数据，修改需要更新的字段
        Map<String, Object> newData = new HashMap<>();
        newData.putAll(super.data()); // 直接用父类的 data() 方法获取所有字段
        newData.put(STEPS, newSteps);
        newData.put(CURRENT_STEP, currentStep().orElse(0) + 1);

        // 返回新的 LocalLifeState 实例
        return new LocalLifeState(newData);
    }
}