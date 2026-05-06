package com.fast.agent.memory.shortterm;

import com.fast.agent.memory.context.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 短期会话记忆管理
 */
@Slf4j
@Component
public class ShortTermMemory {

    /**
     * 会话上下文缓存
     */
    private final ConcurrentHashMap<String, SessionContext> contextCache = new ConcurrentHashMap<>();

    /**
     * 获取或创建会话上下文
     */
    public SessionContext getOrCreateContext(String sessionId, String userId) {
        return contextCache.computeIfAbsent(sessionId, id -> {
            SessionContext context = new SessionContext();
            context.setSessionId(sessionId);
            context.setUserId(userId);
            context.getMetadata().setCreateTime(System.currentTimeMillis());
            context.getMetadata().setMessageCount(0);
            log.info("创建新会话上下文: sessionId={}, userId={}", sessionId, userId);
            return context;
        });
    }

    /**
     * 获取会话上下文
     */
    public SessionContext getContext(String sessionId) {
        return contextCache.get(sessionId);
    }

    /**
     * 移除会话上下文
     */
    public void removeContext(String sessionId) {
        contextCache.remove(sessionId);
        log.info("移除会话上下文: sessionId={}", sessionId);
    }

    /**
     * 添加消息到会话上下文
     */
    public void addMessage(String sessionId, String role, String content) {
        SessionContext context = getContext(sessionId);
        if (context != null) {
            context.addMessage(role, content);
            context.getMetadata().setUpdateTime(System.currentTimeMillis());
            context.getMetadata().setMessageCount(context.getMessages().size());
        }
    }
}
