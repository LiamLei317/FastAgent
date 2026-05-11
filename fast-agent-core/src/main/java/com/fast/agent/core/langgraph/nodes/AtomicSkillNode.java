package com.fast.agent.core.langgraph.nodes;

import com.fast.agent.core.langgraph.state.SkillGraphState;
import com.fast.agent.core.llm.ChatModelFactory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图解析 node
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AtomicSkillNode {
    private final ChatModelFactory chatModelFactory;

    public SkillGraphState execute(SkillGraphState state) {
        String prompt = """
                # 任务说明
                你是意图分类器，请严格根据用户输入的语义，从下方给定的15个原子技能编码中，**选出唯一一个最匹配的编码**。

                # 可选原子技能编码列表
                MARKET_RESEARCH,
                CREATIVE_DESIGN,
                COST_QUOTE,
                PRODUCTION_PLAN,
                RISK_ASSESSMENT,
                CONVERSATION_MANAGE,
                TASK_DECOMPOSE,
                DATA_ANALYSIS,
                COPY_WRITING,
                CUSTOMER_SERVICE,
                SUMMARY,
                TRANSLATION,
                BRAINSTORM,
                CODE,
                DOC_GENERATOR

                # 严格约束规则
                1. 只允许返回列表中存在的**唯一一个编码**，不要输出任何多余解释、标点、换行、说明；
                2. 按照**语义意图**匹配，不要按字面生硬匹配；
                3. 若用户需求模糊、无明确业务倾向、闲聊、问候，统一返回：CONVERSATION_MANAGE；
                4. 禁止编造不在列表里的编码，禁止返回多个编码；
                5. 只返回纯编码文本，不要加任何格式化内容。

                # 用户输入
                %s

                # 输出要求
                仅输出匹配到的单个编码：
                """.formatted(state.getUserInput());

        log.info("AtomicSkillNode-start: {}", state);
        ChatLanguageModel chatModel = chatModelFactory.createChatModel();
        String atomicCode = chatModel.generate(prompt);

        // 新状态
        Map<String, Object> newData = new HashMap<>(state.data());
        newData.put("atomicSkillCode", atomicCode);
        newData.put("retryCount", state.getRetryCount() + 1);
        SkillGraphState newState = new SkillGraphState(newData);
        log.info("AtomicSkillNode-end: {}", newState);
        return newState;
    }
}