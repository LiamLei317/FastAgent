package com.fast.agent.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fast.agent.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
