package com.fast.agent.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    // 参数错误
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(401, "缺少必要参数"),
    PARAM_INVALID(402, "参数格式不正确"),
    
    // 业务错误
    BUSINESS_ERROR(1000, "业务处理失败"),
    
    // Agent 相关错误
    AGENT_ERROR(2000, "Agent 处理失败"),
    AGENT_NOT_FOUND(2001, "Agent 不存在"),
    AGENT_CONFIG_ERROR(2002, "Agent 配置错误"),
    LLM_ERROR(2003, "大模型调用失败"),
    LLM_TIMEOUT(2004, "大模型调用超时"),
    
    // 记忆相关错误
    MEMORY_ERROR(3000, "记忆管理失败"),
    MEMORY_NOT_FOUND(3001, "记忆不存在"),
    
    // 知识库相关错误
    KNOWLEDGE_ERROR(4000, "知识库操作失败"),
    KNOWLEDGE_NOT_FOUND(4001, "知识库不存在"),
    DOCUMENT_PARSE_ERROR(4002, "文档解析失败"),
    VECTOR_ERROR(4003, "向量操作失败"),
    
    // 工具相关错误
    TOOL_ERROR(5000, "工具调用失败"),
    TOOL_NOT_FOUND(5001, "工具不存在"),
    TOOL_EXECUTION_ERROR(5002, "工具执行异常"),
    
    // 会话相关错误
    SESSION_ERROR(6000, "会话管理失败"),
    SESSION_NOT_FOUND(6001, "会话不存在"),
    
    // 系统错误
    SYSTEM_ERROR(9000, "系统内部错误"),
    SERVER_ERROR(9001, "服务器异常");

    private final Integer code;
    private final String message;
}
