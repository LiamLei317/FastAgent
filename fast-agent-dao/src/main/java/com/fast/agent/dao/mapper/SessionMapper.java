package com.fast.agent.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fast.agent.model.entity.Session;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话 Mapper
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
