package com.fast.agent.core.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 阶段完成检测器
 * 用于检测大模型返回内容中是否包含阶段完成标记
 */
@Slf4j
@Component
public class StepCompletionDetector {

    /**
     * 阶段完成标记
     */
    private static final String[] COMPLETION_MARKERS = {
        "**阶段完成**",
        "阶段完成",
        "**STEP_COMPLETED**",
        "STEP_COMPLETED",
        "当前阶段已完成",
        "本阶段完成"
    };

    /**
     * 检测大模型返回内容是否包含阶段完成标记
     * @param response 大模型返回内容
     * @return 是否完成当前阶段
     */
    public boolean isStepCompleted(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }

        for (String marker : COMPLETION_MARKERS) {
            if (response.contains(marker)) {
                log.debug("检测到阶段完成标记：{}", marker);
                return true;
            }
        }

        return false;
    }

    /**
     * 清理大模型返回内容中的完成标记
     * @param response 原始返回内容
     * @return 清理后的内容
     */
    public String cleanCompletionMarkers(String response) {
        if (response == null) {
            return null;
        }

        String cleaned = response;
        for (String marker : COMPLETION_MARKERS) {
            cleaned = cleaned.replace(marker, "").trim();
        }

        return cleaned;
    }
}
