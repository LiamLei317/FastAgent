package com.fast.agent.core.langgraph.demo.queryWeather;


import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WeatherAskState extends AgentState {
    // 1. 字段常量
    public static final String MESSAGES = "messages";

    // 2. Schema：必须！定义合并规则
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES, Channels.appender(ArrayList::new)
    );

    // 3. 构造器：必须！父类要求
    public WeatherAskState(Map<String, Object> initData) {
        super(initData);
    }

    // 4. 正确的 value() 用法（泛型 + Optional）
    public List<String> messages() {
        Optional<List<String>> opt = this.value(MESSAGES);
        return opt.orElse(new ArrayList<>());
    }
}
