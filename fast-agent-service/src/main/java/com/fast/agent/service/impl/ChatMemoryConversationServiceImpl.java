package com.fast.agent.service.impl;

import com.fast.agent.core.chat.ChatAssistant;
import com.fast.agent.core.chat.ChatMemoryService;
import com.fast.agent.service.ChatMemoryConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基于 ChatMemory 的对话服务实现
 * 
 * 核心功能：
 * 1. 封装 ChatMemoryService，提供业务层接口
 * 2. 管理会话级别的 ChatAssistant 实例
 * 3. 提供记忆管理功能
 * 4. 支持多用户隔离
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryConversationServiceImpl implements ChatMemoryConversationService {

    private final ChatMemoryService chatMemoryService;

    @Override
    public ChatAssistant getChatAssistant(String sessionId) {
        log.debug("获取会话 ChatAssistant，sessionId: {}", sessionId);
        return chatMemoryService.getChatAssistant(sessionId);
    }

    @Override
    public ChatAssistant getChatAssistant(String userId, String sessionId) {
        log.debug("获取用户会话 ChatAssistant，userId: {}, sessionId: {}", userId, sessionId);
        return chatMemoryService.getChatAssistant(userId, sessionId);
    }

    @Override
    public void clearSessionMemory(String sessionId) {
        log.info("清除会话记忆，sessionId: {}", sessionId);
        chatMemoryService.clearChatMemory(sessionId);
    }

    @Override
    public void clearSessionMemory(String userId, String sessionId) {
        log.info("清除用户会话记忆，userId: {}, sessionId: {}", userId, sessionId);
        chatMemoryService.clearChatMemory(userId, sessionId);
    }

    @Override
    public String getSessionMemoryInfo(String sessionId) {
        String info = chatMemoryService.getMemoryInfo("default", sessionId);
        log.debug("获取会话记忆信息，sessionId: {}, info: {}", sessionId, info);
        return info;
    }

    @Override
    public String getSessionMemoryInfo(String userId, String sessionId) {
        String info = chatMemoryService.getMemoryInfo(userId, sessionId);
        log.debug("获取用户会话记忆信息，userId: {}, sessionId: {}, info: {}", userId, sessionId, info);
        return info;
    }
}
