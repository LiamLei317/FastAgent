package com.fast.agent.memory.persist;

import com.fast.agent.memory.context.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 记忆持久化服务
 */
@Slf4j
@Service
public class MemoryPersistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final long SESSION_EXPIRE_HOURS = 24;

    public MemoryPersistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 持久化会话上下文到 Redis
     */
    public void persistSessionContext(SessionContext context) {
        String key = SESSION_KEY_PREFIX + context.getSessionId();
        redisTemplate.opsForValue().set(key, context, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("持久化会话上下文: sessionId={}", context.getSessionId());
    }

    /**
     * 从 Redis 加载会话上下文
     */
    public SessionContext loadSessionContext(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        SessionContext context = (SessionContext) redisTemplate.opsForValue().get(key);
        log.info("加载会话上下文: sessionId={}, found={}", sessionId, context != null);
        return context;
    }

    /**
     * 删除会话上下文
     */
    public void deleteSessionContext(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.info("删除会话上下文: sessionId={}", sessionId);
    }
}
