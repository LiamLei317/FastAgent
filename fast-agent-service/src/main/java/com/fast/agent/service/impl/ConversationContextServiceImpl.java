package com.fast.agent.service.impl;

import com.fast.agent.model.entity.Message;
import com.fast.agent.service.ConversationContextService;
import com.fast.agent.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话上下文服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationContextServiceImpl implements ConversationContextService {

    private final StringRedisTemplate redisTemplate;
    private final MessageService messageService;
    
    private static final String CONTEXT_KEY_PREFIX = "chat:context:";
    private static final int DEFAULT_CONTEXT_LIMIT = 5; // 最多保存5轮对话
    private static final Duration EXPIRATION_TIME = Duration.ofHours(24); // 24小时过期

    /**
     * 从数据库加载历史对话到 Redis
     * @param sessionId 会话ID
     */
    private void loadHistoryFromDatabase(String sessionId) {
        try {
            log.info("Redis中未找到会话{}的上下文，从数据库加载历史对话", sessionId);
            
            // 从数据库查询最近4轮对话（8条消息）
            List<Message> messages = messageService.getRecentMessagesBySessionId(Long.parseLong(sessionId), 8);
            
            if (!messages.isEmpty()) {
                String key = CONTEXT_KEY_PREFIX + sessionId;
                
                // 将数据库消息按时间顺序保存到 Redis（旧消息在前，新消息在后）
                for (Message message : messages) {
                    String messageEntry = message.getRole() + ":" + message.getContent();
                    redisTemplate.opsForList().rightPush(key, messageEntry);
                }
                
                // 设置过期时间
                redisTemplate.expire(key, EXPIRATION_TIME);
                
                log.info("从数据库加载了{}条历史消息到Redis，sessionId: {}", messages.size(), sessionId);
            } else {
                log.info("数据库中未找到会话{}的历史消息", sessionId);
            }
        } catch (Exception e) {
            log.error("从数据库加载历史对话失败，sessionId: {}", sessionId, e);
        }
    }

    @Override
    public void saveMessage(String sessionId, String role, String content) {
        try {
            String key = CONTEXT_KEY_PREFIX + sessionId;
            
            // 检查 Redis 中是否存在该会话的上下文
            Boolean exists = redisTemplate.hasKey(key);
            if (exists == null || !exists) {
                // Redis 中没有，从数据库加载历史对话
                loadHistoryFromDatabase(sessionId);
            }
            
            String messageEntry = role + ":" + content;
            
            // 使用 Redis List 存储消息，LPUSH 保证最新的消息在前面
            redisTemplate.opsForList().leftPush(key, messageEntry);
            
            // 限制列表长度，只保留最近的对话
            redisTemplate.opsForList().trim(key, 0, DEFAULT_CONTEXT_LIMIT * 2 - 1);
            
            // 设置过期时间
            redisTemplate.expire(key, EXPIRATION_TIME);
            
            log.debug("保存消息到Redis，sessionId: {}, role: {}", sessionId, role);
        } catch (Exception e) {
            log.error("保存消息到Redis失败，sessionId: {}", sessionId, e);
            // Redis 失败不应该影响对话流程
        }
    }

    @Override
    public List<String> getRecentMessages(String sessionId, int limit) {
        try {
            String key = CONTEXT_KEY_PREFIX + sessionId;
            List<String> messages = redisTemplate.opsForList().range(key, 0, limit * 2 - 1);
            
            if (messages == null) {
                return new ArrayList<>();
            }
            
            log.debug("获取Redis中的消息历史，sessionId: {}, 消息数量: {}", sessionId, messages.size());
            return messages;
        } catch (Exception e) {
            log.error("获取Redis消息历史失败，sessionId: {}", sessionId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void clearHistory(String sessionId) {
        try {
            String key = CONTEXT_KEY_PREFIX + sessionId;
            redisTemplate.delete(key);
            log.info("清除会话历史，sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("清除会话历史失败，sessionId: {}", sessionId, e);
        }
    }

    @Override
    public String formatContext(String sessionId, int limit) {
        List<String> messages = getRecentMessages(sessionId, limit);
        
        if (messages.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("以下是最近的对话历史：\n\n");
        
        // Redis List 中最新的消息在前面，需要反转顺序以保持对话的时间顺序
        for (int i = messages.size() - 1; i >= 0; i--) {
            String messageEntry = messages.get(i);
            String[] parts = messageEntry.split(":", 2);
            
            if (parts.length >= 2) {
                String role = parts[0];
                String content = parts[1];
                
                String roleText = "user".equals(role) ? "用户" : "助手";
                context.append(roleText).append(": ").append(content).append("\n");
            }
        }
        
        context.append("\n基于以上对话历史，请回答用户的最新问题：");
        
        log.debug("格式化上下文完成，sessionId: {}, 上下文长度: {}", sessionId, context.length());
        return context.toString();
    }
}
