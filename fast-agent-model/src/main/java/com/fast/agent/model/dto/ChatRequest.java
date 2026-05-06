package com.fast.agent.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户消息
     */
    @NotBlank(message = "用户消息不能为空")
    private String message;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 是否流式输出
     */
    private Boolean stream = false;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大Token数
     */
    private Integer maxTokens;
}
