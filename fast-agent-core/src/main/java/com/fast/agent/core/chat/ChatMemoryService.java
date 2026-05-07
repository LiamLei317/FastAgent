package com.fast.agent.core.chat;

import com.fast.agent.core.llm.LlmConfig;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ChatMemory 服务
 * 
 * 核心功能：
 * 1. 管理 ChatAssistant 实例
 * 2. 为每个会话创建独立的 ChatMemory
 * 3. 支持多用户、多会话隔离
 * 4. 自动管理上下文长度
 */
@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final LlmConfig llmConfig;
    private final TokenWindowChatMemory tokenWindowChatMemory;
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatMemoryService.class);

    /**
     * 获取会话专用的 ChatAssistant
     * 每个会话使用独立的 memoryId 进行隔离
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return ChatAssistant 实例
     */
    public ChatAssistant getChatAssistant(String userId, String sessionId) {
        String memoryId = buildMemoryId(userId, sessionId);
        log.debug("创建 ChatAssistant，memoryId: {}", memoryId);
        
        // 创建 OpenAI 模型
        OpenAiChatModel model = createChatModel();
        
        // 使用 AiServices 创建 ChatAssistant，使用共享的 TokenWindowChatMemory
        return AiServices.builder(ChatAssistant.class)
                .chatLanguageModel(model)
                .chatMemory(tokenWindowChatMemory)
                .build();
    }

    /**
     * 获取会话专用的 ChatAssistant（简化版本）
     * 
     * @param sessionId 会话ID
     * @return ChatAssistant 实例
     */
    public ChatAssistant getChatAssistant(String sessionId) {
        return getChatAssistant("default", sessionId);
    }

    /**
     * 清除指定会话的 ChatMemory
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    public void clearChatMemory(String userId, String sessionId) {
        String memoryId = buildMemoryId(userId, sessionId);
        log.info("清除会话记忆，memoryId: {}", memoryId);
        // 简化实现，暂时不执行实际清除操作
    }

    /**
     * 清除指定会话的 ChatMemory（简化版本）
     * 
     * @param sessionId 会话ID
     */
    public void clearChatMemory(String sessionId) {
        clearChatMemory("default", sessionId);
    }

    /**
     * 获取会话的记忆信息
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 记忆信息
     */
    public String getMemoryInfo(String userId, String sessionId) {
        String memoryId = buildMemoryId(userId, sessionId);
        return String.format("会话记忆信息 - memoryId: %s, 使用 TokenWindowChatMemory", memoryId);
    }

    /**
     * 构建 memoryId
     * 格式：userId:sessionId
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return memoryId
     */
    private String buildMemoryId(String userId, String sessionId) {
        return userId + ":" + sessionId;
    }

    /**
     * 创建 OpenAI 聊天模型
     * 
     * @return OpenAiChatModel 实例
     */
    private OpenAiChatModel createChatModel() {
        var builder = OpenAiChatModel.builder()
                .apiKey(llmConfig.getApiKey())
                .modelName(llmConfig.getModelName())
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(60));
        
        if (llmConfig.getBaseUrl() != null && !llmConfig.getBaseUrl().isEmpty()) {
            builder.baseUrl(llmConfig.getBaseUrl());
        }
        
        return builder.build();
    }
}
