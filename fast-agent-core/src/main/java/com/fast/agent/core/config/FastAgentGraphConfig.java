package com.fast.agent.core.config;

import com.fast.agent.common.constant.BaseNodeConstant;
import com.fast.agent.core.langgraph.nodes.*;
import com.fast.agent.core.langgraph.nodes.base.AnswerNode;
import com.fast.agent.core.langgraph.nodes.base.SkillNode;
import com.fast.agent.core.langgraph.nodes.base.ThinkNode;
import com.fast.agent.core.langgraph.nodes.base.ToolExecuteNode;
import com.fast.agent.core.langgraph.state.base.FastAgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Configuration
@RequiredArgsConstructor
public class FastAgentGraphConfig {

    // 注入所有节点
    private final ThinkNode thinkNode;
    private final SkillNode skillNode;
    private final ToolExecuteNode toolExecuteNode;
    private final AnswerNode answerNode;

    // ====================== ✅ 这里就是图的初始化逻辑 ======================
    @Bean
    public CompiledGraph<FastAgentState> fastAgentGraph() throws GraphStateException {
        // 1. 状态工厂
        AgentStateFactory<FastAgentState> stateFactory = FastAgentState::new;

        // 2. 创建图
        StateGraph<FastAgentState> graph = new StateGraph<>(stateFactory);

        // 3. 添加节点（绑定 node.execute()）
        graph.addNode(BaseNodeConstant.THINK_NODE,          state -> executeNode(thinkNode::execute, state));
        graph.addNode(BaseNodeConstant.SKILL_NODE,          state -> executeNode(skillNode::execute, state));
        graph.addNode(BaseNodeConstant.TOOL_EXECUTE_NODE,   state -> executeNode(toolExecuteNode::execute, state));
        graph.addNode(BaseNodeConstant.ANSWER_NODE,state -> executeNode(answerNode::execute, state));

        // 4. 流程编排
        graph.addEdge(START, BaseNodeConstant.THINK_NODE);
        graph.addEdge(BaseNodeConstant.THINK_NODE, BaseNodeConstant.SKILL_NODE);
        graph.addEdge(BaseNodeConstant.SKILL_NODE, BaseNodeConstant.TOOL_EXECUTE_NODE);
        graph.addEdge(BaseNodeConstant.TOOL_EXECUTE_NODE, BaseNodeConstant.ANSWER_NODE);
        graph.addEdge(BaseNodeConstant.ANSWER_NODE, END);

        // 5. 编译完成
        return graph.compile();
    }

    // ====================== 工具方法：统一执行节点 ======================
    private CompletableFuture<Map<String, Object>> executeNode(
            Function<FastAgentState, FastAgentState> nodeFunc,
            FastAgentState state
    ) {
        try {
            FastAgentState newState = nodeFunc.apply(state);
            return CompletableFuture.completedFuture(newState.data());
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
            f.completeExceptionally(e);
            return f;
        }
    }
}
