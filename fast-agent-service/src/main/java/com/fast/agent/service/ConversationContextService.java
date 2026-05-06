package com.fast.agent.service;

import java.util.List;

/**
 * 对话上下文服务接口
 */
public interface ConversationContextService {

    /**
     * 保存对话消息到 Redis
     * @param sessionId 会话ID
     * @param role 消息角色 (user/assistant)
     * @param content 消息内容
     */
    void saveMessage(String sessionId, String role, String content);

    /**
     * 获取最近的对话历史
     * @param sessionId 会话ID
     * @param limit 获取最近的消息数量
     * @return 消息列表
     */
    List<String> getRecentMessages(String sessionId, int limit);

    /**
     * 清除会话的对话历史
     * @param sessionId 会话ID
     */
    void clearHistory(String sessionId);

    /**
     * 格式化对话历史为上下文
     * @param sessionId 会话ID
     * @param limit 最近消息数量
     * @return 格式化的上下文字符串
     */
    String formatContext(String sessionId, int limit);
}
