package com.fast.agent.core.intent;

import com.fast.agent.core.llm.ChatModelFactory;
import com.fast.agent.model.enums.IntentType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntentParser {

    private static final String INTENT_RECOGNITION_PROMPT = """
            # 身份
            你是一个100%%精准、零错误的用户意图分类器。
            你只做分类，不回答问题，不解释，不自由发挥。
            必须严格按规则输出，不允许任何错误。

            # 【强制规则：优先级最高】
            只要用户的问题符合以下任意一条，**直接判定为对应意图**：

            ---
            ## 【1】CREATIVE_CLARIFICATION → 创意澄清
            触发条件（满足任意一条）：
            - 用户有想法、点子、创意
            - 用户描述一个产品，但不清晰、不完整
            - 用户想把想法变清晰
            - 用户说：我有一个想法、我想做个东西、不知道怎么做、帮我完善想法

            关键词：想法、创意、点子、完善、理清、明确、不知道怎么做
            ---

            ## 【2】MARKET_ANALYSIS → 市场验证/市场分析
            触发条件（满足任意一条）：
            - 用户想做一个产品
            - 用户想开发/设计/创业
            - 用户问：有没有市场、赚不赚钱、有没有前景
            - 用户问：竞品、市场规模、可行性、商业模式
            - 用户说：我想做AI花盆、我想做智能设备、我想做一款产品

            关键词：我想做、我想开发、我想设计、创业、市场、前景、可行性、竞品、能不能成
            ---

            ## 【3】PRODUCT_DEFINITION → 产品定义
            触发条件（满足任意一条）：
            - 用户要明确产品功能
            - 用户要做产品定位、目标用户
            - 用户要梳理核心价值、使用场景
            - 用户要确定产品到底是什么、有什么用

            关键词：产品定义、功能、定位、用户、场景、核心价值、产品是什么
            ---

            ## 【4】CREATIVE_DESIGN → 设计选择
            触发条件（满足任意一条）：
            - 用户要做外观设计、界面设计、交互设计
            - 用户问：长什么样、怎么设计、用什么技术、怎么实现
            - 用户要做产品样式、结构、外形、配色、风格

            关键词：设计、外观、界面、交互、长什么样、技术方案、怎么实现
            ---

            ## 【5】DEFAULT → 其他所有内容
            问候、聊天、无关问题。

            # 【输出格式（必须严格遵守，一个字都不能改）】
            INTENT: 意图名称

            # 【示例】
            用户：我想做一个AI花盆
            输出：INTENT: market-analysis

            用户：我有一个创意，帮我理清
            输出：INTENT: creative-clarification

            用户：帮我定义这个产品功能
            输出：INTENT: product-definition

            用户：这个产品该怎么设计
            输出：INTENT: creative-design

            用户：你好
            输出：INTENT: default

            # 现在，请严格分类以下用户问题
            用户问题：%s
            """;

    private final ChatModelFactory chatModelFactory;

    private IntentRecognitionAssistant assistant;

    @PostConstruct
    public void init() {
        ChatLanguageModel model = chatModelFactory.createChatModel();
        // AI Service 声明式抽象， langchain4j 动态代理自动实现 IntentRecognitionAssistant 接口
        this.assistant = AiServices.builder(IntentRecognitionAssistant.class)
                .chatLanguageModel(model)
                .build();
        log.info("IntentParser 初始化完成");
    }

    public IntentType parseIntent(String userQuestion) {
        try {
            log.info("开始解析意图：{}", userQuestion);
            String prompt = String.format(INTENT_RECOGNITION_PROMPT, userQuestion);
            String response = assistant.recognizeIntent(prompt);
            String intentCode = extractIntentCode(response);
            IntentType result = IntentType.fromCode(intentCode);
            log.info("意图识别结果：{}", result);
            return result;
        } catch (Exception e) {
            log.error("意图识别失败，降级为 DEFAULT", e);
            return IntentType.DEFAULT;
        }
    }

    private String extractIntentCode(String response) {
        if (response == null || response.isBlank()) {
            return "DEFAULT";
        }

        for (String line : response.split("\n")) {
            String trim = line.trim();
            if (trim.startsWith("INTENT:")) {
                return trim.substring("INTENT:".length()).trim();
            }
        }

        String upper = response.toUpperCase();
        for (IntentType intent : IntentType.values()) {
            if (upper.contains(intent.getCode().toUpperCase())) {
                return intent.getCode();
            }
        }

        log.warn("无法解析意图：{}", response);
        return "DEFAULT";
    }

    /**
     * 内部接口，用于意图识别
     */
    interface IntentRecognitionAssistant {
        String recognizeIntent(String prompt);
    }
}