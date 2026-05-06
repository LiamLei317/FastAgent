package com.fast.agent.tools.builtin;

import com.fast.agent.tools.base.BaseTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具
 */
@Slf4j
@Component
public class TimeTool extends BaseTool {

    public TimeTool() {
        this.name = "time";
        this.description = "获取当前时间";
    }

    @Override
    public String execute(String input) {
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("时间工具执行: result={}", formatted);
        return formatted;
    }
}
