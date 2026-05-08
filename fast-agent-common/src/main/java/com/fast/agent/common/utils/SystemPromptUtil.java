package com.fast.agent.common.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
public class SystemPromptUtil {

    @Value("classpath:prompts/system_prompt.txt")
    private Resource systemResource;

    public static String SYSTEM_PROMPT;

    @PostConstruct
    public void load() throws Exception {
        SYSTEM_PROMPT = Files.readString(
                systemResource.getFile().toPath(),
                StandardCharsets.UTF_8
        );
    }

    // 对外统一获取入口
    public static String get() {
        return SYSTEM_PROMPT;
    }
}
