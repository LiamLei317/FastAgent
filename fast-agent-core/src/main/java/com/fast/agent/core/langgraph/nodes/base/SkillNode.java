package com.fast.agent.core.langgraph.nodes.base;

import com.fast.agent.common.constant.BaseNodeConstant;
import com.fast.agent.core.langgraph.state.base.FastAgentState;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
public class SkillNode {
    public FastAgentState execute(FastAgentState state) {
        return state
                .withSkillCode("DEFAULT_AGENT")
                .withSystemPrompt("你是一个专业的智能助手");
    }
}