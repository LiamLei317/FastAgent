package com.fast.agent.service;

import com.fast.agent.model.dto.ChatRequest;
import java.util.function.Consumer;

/**
 * 流式对话服务接口
 */
public interface ChatService {

    /**
     * 流式对话
     */
    void streamChat(ChatRequest request, Consumer<String> chunkConsumer);
}
