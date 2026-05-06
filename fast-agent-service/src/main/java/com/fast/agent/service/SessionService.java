package com.fast.agent.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.agent.model.entity.Session;

import java.util.List;

/**
 * 会话服务接口
 */
public interface SessionService {

    /**
     * 创建会话
     * @param session 会话信息
     * @return 创建的会话
     */
    Session createSession(Session session);

    /**
     * 根据ID获取会话
     * @param id 会话ID
     * @return 会话信息
     */
    Session getSessionById(Long id);

    /**
     * 根据用户ID获取会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Session> getSessionsByUserId(String userId);

    /**
     * 分页获取用户会话列表
     * @param userId 用户ID
     * @param current 当前页
     * @param size 页大小
     * @return 分页结果
     */
    Page<Session> getSessionsByUserIdPage(String userId, Integer current, Integer size);

    /**
     * 更新会话
     * @param session 会话信息
     * @return 更新结果
     */
    boolean updateSession(Session session);

    /**
     * 删除会话
     * @param id 会话ID
     * @return 删除结果
     */
    boolean deleteSession(Long id);

    /**
     * 根据用户ID删除所有会话
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteSessionsByUserId(String userId);
}
