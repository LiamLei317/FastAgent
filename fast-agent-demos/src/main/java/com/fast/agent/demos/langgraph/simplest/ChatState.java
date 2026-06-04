package com.fast.agent.demos.langgraph.simplest;

import lombok.Data;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChatState extends AgentState {
    public static final String MESSAGES = "messages";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES, Channels.appender(ArrayList::new)
    );

    public ChatState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> messages() {
        Optional<List<String>> opt = this.value(MESSAGES);
        return opt.orElse(new ArrayList<>());
    }
}