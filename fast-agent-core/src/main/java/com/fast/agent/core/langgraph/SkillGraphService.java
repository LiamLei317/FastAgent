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

    public String run(String userQuery) throws Exception {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("userInput", userQuery); // 对应你 State 里的字段名

        Optional<SkillGraphState> finalStateOpt = skillGraph.invoke(inputs);
        SkillGraphState finalState = finalStateOpt.get();

        return finalState.getFinalSystemPrompt();
    }
}
