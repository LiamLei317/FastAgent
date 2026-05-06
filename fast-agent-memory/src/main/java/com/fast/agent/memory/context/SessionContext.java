package com.fast.agent.memory.context;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话上下文容器
 */
@Data
public class SessionContext {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 消息历史
     */
    private List<Message> messages = new ArrayList<>();

    /**
     * 上下文元数据
     */
    private Metadata metadata = new Metadata();

    /**
     * 添加消息
     */
    public void addMessage(String role, String content) {
        messages.add(new Message(role, content));
    }

    /**
     * 获取消息历史
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * 清空消息历史
     */
    public void clearMessages() {
        messages.clear();
    }

    /**
     * 消息类
     */
    @Data
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * 元数据类
     */
    @Data
    public static class Metadata {
        private Long createTime;
        private Long updateTime;
        private Integer messageCount;
    }
}
