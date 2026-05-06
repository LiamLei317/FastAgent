package com.fast.agent.core.prompt;

/**
 * Prompt 模板管理
 */
public class PromptTemplate {

    /**
     * 默认系统提示词
     */
    public static final String DEFAULT_SYSTEM_PROMPT = """
        You are a helpful AI assistant designed to assist users with various tasks.
        You should provide clear, accurate, and helpful responses.
        """;

    /**
     * 聊天提示词模板
     */
    public static String chatPrompt(String userMessage) {
        return """
            %s
            
            User: %s
            Assistant:
            """.formatted(DEFAULT_SYSTEM_PROMPT, userMessage);
    }

    /**
     * 自定义系统提示词模板
     */
    public static String customSystemPrompt(String systemPrompt, String userMessage) {
        return """
            %s
            
            User: %s
            Assistant:
            """.formatted(systemPrompt, userMessage);
    }

    private PromptTemplate() {
    }
}
