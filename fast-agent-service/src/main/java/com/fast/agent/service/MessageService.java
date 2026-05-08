package com.fast.agent.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.agent.model.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 创建消息
     * @param message 消息信息
     * @return 创建的消息
     */
    Message createMessage(Message message);

    /**
     * 根据ID获取消息
     * @param id 消息ID
     * @return 消息信息
     */
    Message getMessageById(Long id);

    /**
     * 根据会话ID获取消息列表
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<Message> getMessagesBySessionId(String sessionId);

    /**
     * 分页获取会话消息列表
     * @param sessionId 会话ID
     * @param current 当前页
     * @param size 页大小
     * @return 分页结果
     */
    Page<Message> getMessagesBySessionIdPage(Long sessionId, Integer current, Integer size);

    /**
     * 更新消息
     * @param message 消息信息
     * @return 更新结果
     */
    boolean updateMessage(Message message);

    /**
     * 删除消息
     * @param id 消息ID
     * @return 删除结果
     */
    boolean deleteMessage(Long id);

    /**
     * 根据会话ID删除所有消息
     * @param sessionId 会话ID
     * @return 删除结果
     */
    boolean deleteMessagesBySessionId(Long sessionId);

    /**
     * 获取会话消息数量
     * @param sessionId 会话ID
     * @return 消息数量
     */
    Long getMessageCountBySessionId(Long sessionId);

    /**
     * 根据会话ID获取最近的N条消息
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<Message> getRecentMessagesBySessionId(Long sessionId, Integer limit);
}
