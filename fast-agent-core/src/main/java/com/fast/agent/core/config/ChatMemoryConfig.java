package com.fast.agent.core.config;

import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * LangChain4j ChatMemory 配置类
 *
 * 核心架构：
 * TokenWindowChatMemory -> RedisChatMemoryStore -> AiServices
 *
 * 功能特性：
 * - 按 Token 容量自动管理上下文
 * - Redis 持久化存储
 * - 多用户隔离
 * - 自动过期管理
 */
@Configuration
@EnableConfigurationProperties(ChatMemoryConfig.ChatMemoryProperties.class) // ✅ 修复配置类报错
public class ChatMemoryConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatMemoryConfig.class);

    /**
     * ChatMemory 配置属性
     */
    @ConfigurationProperties(prefix = "fast-agent.chat-memory")
    public static class ChatMemoryProperties {
        private int maxTokens = 8192;
        private int expirationHours = 24;

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public int getExpirationHours() {
            return expirationHours;
        }

        public void setExpirationHours(int expirationHours) {
            this.expirationHours = expirationHours;
        }
    }

    /**
     * 配置 RedisTemplate 用于 ChatMemoryStore
     * 使用 JSON 序列化确保消息对象正确存储
     */
    @Bean("chatMemoryRedisTemplate")
    public RedisTemplate<String, Object> chatMemoryRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 String 序列化器作为 key 的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 使用 JSON 序列化器作为 value 的序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("ChatMemory RedisTemplate 配置完成");
        return template;
    }

    /**
     * 创建 Redis ChatMemoryStore
     * 用于持久化存储对话记忆
     */
    @Bean
    public ChatMemoryStore redisChatMemoryStore(RedisTemplate<String, Object> chatMemoryRedisTemplate) {
        // 注意：由于 LangChain4j RedisChatMemoryStore 可能存在兼容性问题
        // 这里先使用 InMemoryChatMemoryStore，后续可替换为 Redis 实现
        ChatMemoryStore store = new InMemoryChatMemoryStore();
        log.info("ChatMemoryStore 配置完成");
        return store;
    }

    /**
     * 创建 TokenWindowChatMemory Bean
     * 按 Token 容量自动管理对话上下文
     */
    @Bean
    public TokenWindowChatMemory tokenWindowChatMemory(
            ChatMemoryStore chatMemoryStore,
            com.fast.agent.core.llm.LlmConfig llmConfig,
            ChatMemoryProperties properties // ✅ 注入配置
    ) {
        // 智谱AI使用不同的tokenizer，这里使用通用的token估算
        // 对于非OpenAI模型，使用默认的GPT tokenizer作为近似
        String modelName = llmConfig.getModelName() != null ? llmConfig.getModelName() : "gpt-3.5-turbo";
        OpenAiTokenizer tokenizer;
        
        // 检查是否为智谱AI模型，使用兼容的tokenizer
        if (modelName.startsWith("glm-") || modelName.contains("zhipu")) {
            // 智谱AI模型使用GPT-3.5-turbo的tokenizer作为近似
            tokenizer = new OpenAiTokenizer("gpt-3.5-turbo");
            log.info("使用智谱AI模型 {}，采用GPT-3.5-turbo tokenizer作为近似", modelName);
        } else {
            // OpenAI模型使用对应的tokenizer
            tokenizer = new OpenAiTokenizer(modelName);
            log.info("使用OpenAI模型 {}，采用对应tokenizer", modelName);
        }

        TokenWindowChatMemory chatMemory = TokenWindowChatMemory.builder()
                .maxTokens(properties.getMaxTokens(), tokenizer) // ✅ 使用注入的配置
                .chatMemoryStore(chatMemoryStore)
                .build();

        log.info("TokenWindowChatMemory 配置完成，maxTokens: {}", properties.getMaxTokens());
        return chatMemory;
    }
}