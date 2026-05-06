package com.fast.agent.common.exception;

import com.fast.agent.common.result.ResultCode;

/**
 * Agent 专属异常
 */
public class AgentException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AgentException(String message) {
        super(ResultCode.AGENT_ERROR.getCode(), message);
    }

    public AgentException(ResultCode resultCode) {
        super(resultCode);
    }

    public AgentException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public AgentException(String message, Throwable cause) {
        super(ResultCode.AGENT_ERROR.getCode(), message, cause);
    }
}
