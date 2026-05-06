package com.fast.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fast.agent.dao.mapper.MessageMapper;
import com.fast.agent.model.entity.Message;
import com.fast.agent.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public Message createMessage(Message message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(LocalDateTime.now());
        }
        messageMapper.insert(message);
        log.info("创建消息成功，ID: {}, 会话ID: {}", message.getId(), message.getSessionId());
        return message;
    }

    @Override
    public Message getMessageById(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            log.warn("未找到消息，ID: {}", id);
        }
        return message;
    }

    @Override
    public List<Message> getMessagesBySessionId(Long sessionId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                   .orderByAsc("create_time");
        List<Message> messages = messageMapper.selectList(queryWrapper);
        log.info("会话 {} 的消息列表，共 {} 条记录", sessionId, messages.size());
        return messages;
    }

    @Override
    public Page<Message> getMessagesBySessionIdPage(Long sessionId, Integer current, Integer size) {
        Page<Message> page = new Page<>(current != null ? current : 1, size != null ? size : 20);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                   .orderByAsc("create_time");
        Page<Message> result = messageMapper.selectPage(page, queryWrapper);
        log.info("会话 {} 的消息分页查询，第 {} 页，每页 {} 条，共 {} 条记录", 
                 sessionId, current, size, result.getTotal());
        return result;
    }

    @Override
    public boolean updateMessage(Message message) {
        int result = messageMapper.updateById(message);
        if (result > 0) {
            log.info("更新消息成功，ID: {}", message.getId());
            return true;
        } else {
            log.warn("更新消息失败，ID: {}", message.getId());
            return false;
        }
    }

    @Override
    public boolean deleteMessage(Long id) {
        int result = messageMapper.deleteById(id);
        if (result > 0) {
            log.info("删除消息成功，ID: {}", id);
            return true;
        } else {
            log.warn("删除消息失败，ID: {}", id);
            return false;
        }
    }

    @Override
    public boolean deleteMessagesBySessionId(Long sessionId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        int result = messageMapper.delete(queryWrapper);
        if (result > 0) {
            log.info("删除会话 {} 的所有消息成功，共 {} 条记录", sessionId, result);
            return true;
        } else {
            log.warn("删除会话 {} 的所有消息失败", sessionId);
            return false;
        }
    }

    @Override
    public Long getMessageCountBySessionId(Long sessionId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        Long count = messageMapper.selectCount(queryWrapper);
        log.info("会话 {} 的消息数量: {}", sessionId, count);
        return count;
    }

    @Override
    public List<Message> getRecentMessagesBySessionId(Long sessionId, Integer limit) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                   .orderByDesc("create_time")  // 按时间倒序，最新的在前
                   .last("LIMIT " + limit);     // 限制数量
        List<Message> messages = messageMapper.selectList(queryWrapper);
        
        // 反转列表，使消息按时间正序排列（旧消息在前，新消息在后）
        java.util.Collections.reverse(messages);
        
        log.info("会话 {} 的最近 {} 条消息", sessionId, messages.size());
        return messages;
    }
}
