package com.fast.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class Message {

    /**
     * 主键ID（自增，保持不变）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID（关键：改成 String）
     */
    private String sessionId; // 这里必须改！

    /**
     * 消息角色：user / assistant / system
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 0-文本 1-图片 2-文件
     */
    private Integer contentType;

    /**
     * 状态 1-正常 0-已删除/撤回
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}