package com.fast.agent.web.sse;

import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.model.entity.Session;
import com.fast.agent.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 流式聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseChatController {

    private final SessionService sessionService;

    /**
     * SSE 流式聊天
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String sessionId, @RequestParam String message) {
        SseEmitter emitter = new SseEmitter(30000L);

        new Thread(() -> {
            try {
                // 校验 sessionId 是否存在
                Session session = sessionService.getSessionById(Long.parseLong(sessionId));
                if (session == null) {
                    emitter.send(SseEmitter.event().data("会话不存在，sessionId: " + sessionId));
                    emitter.complete();
                    return;
                }

                log.info("SSE 流式聊天开始，sessionId: {}, message: {}", sessionId, message);
                
                // TODO: 实现聊天功能，需要创建聊天服务
                emitter.send(SseEmitter.event().data("SSE 聊天功能待实现"));
                emitter.complete();
            } catch (NumberFormatException e) {
                log.error("sessionId 格式错误: {}", sessionId, e);
                try {
                    emitter.send(SseEmitter.event().data("sessionId 格式错误，必须是数字"));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            } catch (Exception e) {
                log.error("SSE 聊天异常", e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
