package com.fast.agent.core.langgraph.nodes.base;

import com.fast.agent.core.langgraph.state.base.FastAgentState;
import org.springframework.stereotype.Component;


@Component
public class AnswerNode {
    public FastAgentState execute(FastAgentState state) {
        String answer = "已处理完成：" + state.getUserQuery();
        return state
                .withFinalAnswer(answer)
                .withIsEnd(true);
    }
}