package com.fast.agent.core.langgraph.nodes.base;

import com.fast.agent.core.langgraph.state.base.FastAgentState;
import org.bsc.langgraph4j.internal.node.Node;

public abstract class BaseAgentNode extends Node<FastAgentState> {

    public BaseAgentNode(String id, ActionFactory<FastAgentState> actionFactory) {
        super(id, actionFactory);
    }
    public abstract String getNodeName();
}
