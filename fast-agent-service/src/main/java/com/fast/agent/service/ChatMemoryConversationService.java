package com.fast.agent.service;

import com.fast.agent.core.chat.ChatAssistant;

/**
 * 基于 ChatMemory 的对话服务接口
 * 
 * 替代原有的 ConversationContextService
 * 使用 LangChain4j 标准 ChatMemory 架构
 */
public interface ChatMemoryConversationService {

    /**
     * 获取会话专用的 ChatAssistant
     * 
     * @param sessionId 会话ID
     * @return ChatAssistant 实例
     */
    ChatAssistant getChatAssistant(String sessionId);

    /**
     * 获取用户会话专用的 ChatAssistant
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return ChatAssistant 实例
     */
    ChatAssistant getChatAssistant(String userId, String sessionId);

    /**
     * 清除会话记忆
     * 
     * @param sessionId 会话ID
     */
    void clearSessionMemory(String sessionId);

    /**
     * 清除用户会话记忆
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void clearSessionMemory(String userId, String sessionId);

    /**
     * 获取会话记忆信息
     * 
     * @param sessionId 会话ID
     * @return 记忆信息
     */
    String getSessionMemoryInfo(String sessionId);

    /**
     * 获取用户会话记忆信息
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 记忆信息
     */
    String getSessionMemoryInfo(String userId, String sessionId);
}
