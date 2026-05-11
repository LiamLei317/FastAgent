package com.fast.agent.core.langgraph;

import com.fast.agent.core.langgraph.state.SkillGraphState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SkillGraphService {
    private final CompiledGraph<SkillGraphState> skillGraph;

    public String run(String userQuery) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("userInput", userQuery); // 对应你 State 里的字段名

        SkillGraphState finalState = skillGraph.invoke(inputs)
                .orElseThrow(() -> new IllegalStateException("LangGraph-Skill 执行未返回任何状态"));

        return finalState.getFinalSystemPrompt();
    }
}
