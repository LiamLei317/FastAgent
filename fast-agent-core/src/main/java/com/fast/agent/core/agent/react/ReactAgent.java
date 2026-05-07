package com.fast.agent.core.agent.react;

import com.fast.agent.core.agent.base.BaseAgent;
import com.fast.agent.core.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ReAct 智能体实现
 */
@Component
@RequiredArgsConstructor
public class ReactAgent extends BaseAgent {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReactAgent.class);

    private final LlmService llmService;

    @Override
    public String execute(String input) {
        log.info("ReactAgent 执行任务: {}", input);
        
        // 构建 ReAct 提示词
        String prompt = buildReactPrompt(input);
        
        // 调用 LLM
        String response = llmService.chat(prompt);
        
        log.info("ReactAgent 响应: {}", response);
        return response;
    }

    /**
     * 构建 ReAct 提示词
     */
    private String buildReactPrompt(String input) {
        return """
            You are a helpful assistant that uses the ReAct (Reasoning + Acting) approach.
            
            Question: %s
            
            Think step by step and provide your reasoning before taking any action.
            """.formatted(input);
    }
}
