package com.fast.agent.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fast.agent.dao.mapper.SessionMapper;
import com.fast.agent.model.entity.Session;
import com.fast.agent.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

    private final SessionMapper sessionMapper;

    @Override
    public Session createSession(Session session) {
        if (session.getCreateTime() == null) {
            session.setCreateTime(LocalDateTime.now());
        }
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        String id = "se_" + IdUtil.getSnowflakeNextIdStr();
        session.setId(id);
        sessionMapper.insert(session);
        log.info("创建会话成功，ID: {}", session.getId());
        return session;
    }

    @Override
    public Session getSessionById(String id) {
        Session session = sessionMapper.selectById(id);
        if (session == null) {
            log.warn("未找到会话，ID: {}", id);
        }
        return session;
    }

    @Override
    public List<Session> getSessionsByUserId(String userId) {
        QueryWrapper<Session> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("update_time");
        List<Session> sessions = sessionMapper.selectList(queryWrapper);
        log.info("用户 {} 的会话列表，共 {} 条记录", userId, sessions.size());
        return sessions;
    }

    @Override
    public Page<Session> getSessionsByUserIdPage(String userId, Integer current, Integer size) {
        Page<Session> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        QueryWrapper<Session> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("update_time");
        Page<Session> result = sessionMapper.selectPage(page, queryWrapper);
        log.info("用户 {} 的会话分页查询，第 {} 页，每页 {} 条，共 {} 条记录", 
                 userId, current, size, result.getTotal());
        return result;
    }

    @Override
    public boolean updateSession(Session session) {
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        int result = sessionMapper.updateById(session);
        if (result > 0) {
            log.info("更新会话成功，ID: {}", session.getId());
            return true;
        } else {
            log.warn("更新会话失败，ID: {}", session.getId());
            return false;
        }
    }

    @Override
    public boolean deleteSession(Long id) {
        int result = sessionMapper.deleteById(id);
        if (result > 0) {
            log.info("删除会话成功，ID: {}", id);
            return true;
        } else {
            log.warn("删除会话失败，ID: {}", id);
            return false;
        }
    }

    @Override
    public boolean deleteSessionsByUserId(String userId) {
        QueryWrapper<Session> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        int result = sessionMapper.delete(queryWrapper);
        if (result > 0) {
            log.info("删除用户 {} 的所有会话成功，共 {} 条记录", userId, result);
            return true;
        } else {
            log.warn("删除用户 {} 的所有会话失败", userId);
            return false;
        }
    }
}
