package com.fast.agent.web.controller;

import com.fast.agent.common.utils.SseEmitterUtils;
import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class AIChatController {

    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request) {
        // 创建统一的emitter
        SseEmitter emitter = SseEmitterUtils.create();

        // 交给service异步处理
        chatService.streamChat(request, emitter);

        // 返回长连接
        return emitter;
    }

}
