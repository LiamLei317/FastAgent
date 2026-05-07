package com.fast.agent.core.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * LangChain4j ChatAssistant 接口
 * 
 * 使用 AiServices 框架定义标准对话服务
 * 自动集成 ChatMemory 进行上下文管理
 */
public interface ChatAssistant {

    /**
     * 基础对话接口
     * 自动携带 ChatMemory 中的历史对话上下文
     * 
     * @param userMessage 用户消息
     * @return AI 回复
     */
    String chat(@UserMessage String userMessage);

    /**
     * 带系统指令的对话接口
     * 用于技能驱动的对话场景
     * 
     * @param systemPrompt 系统指令（技能Prompt）
     * @param userMessage 用户消息
     * @return AI 回复
     */
    String chatWithSystem(
            @V("system") String systemPrompt,
            @UserMessage String userMessage
    );

    /**
     * 流式对话接口
     * 支持流式返回 AI 回复
     * 
     * @param userMessage 用户消息
     * @return 流式 AI 回复
     */
    String streamChat(@UserMessage String userMessage);

    /**
     * 带系统指令的流式对话接口
     * 用于技能驱动的流式对话场景
     * 
     * @param systemPrompt 系统指令（技能Prompt）
     * @param userMessage 用户消息
     * @return 流式 AI 回复
     */
    String streamChatWithSystem(
            @V("system") String systemPrompt,
            @UserMessage String userMessage
    );
}
