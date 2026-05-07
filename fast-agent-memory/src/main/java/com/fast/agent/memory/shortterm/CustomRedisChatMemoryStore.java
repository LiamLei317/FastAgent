package com.fast.agent.memory.shortterm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CustomRedisChatMemoryStore implements ChatMemoryStore {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final long EXPIRE_MINUTES = 15;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = "chat:memory:" + memoryId;
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            return List.of();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = "chat:memory:" + memoryId;
        try {
            String json = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForValue().set(key, json, EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {}
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = "chat:memory:" + memoryId;
        redisTemplate.delete(key);
    }
}
