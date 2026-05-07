package com.fast.agent.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
public class SseEmitterUtils {

    /**
     * 全局统一超时时间：60秒
     */
    private static final long DEFAULT_TIMEOUT = 60 * 1000L;

    /**
     * 创建全局统一配置的 SseEmitter
     */
    public static SseEmitter create() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 统一完成回调
        emitter.onCompletion(() -> {
            log.info("【SSE】连接完成");
        });

        // 统一超时回调
        emitter.onTimeout(() -> {
            log.warn("【SSE】连接超时");
            emitter.complete();
        });

        // 统一异常回调
        emitter.onError(ex -> {
            log.error("【SSE】连接发生异常", ex);
        });

        return emitter;
    }
}
