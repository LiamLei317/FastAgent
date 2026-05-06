package com.fast.agent.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功返回（无数据）
     */
    public static <T> R<T> success() {
        return success(null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> R<T> success(T data) {
        return success(ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回（带消息和数据）
     */
    public static <T> R<T> success(String message, T data) {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(message);
        r.setData(data);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    /**
     * 失败返回（使用默认失败消息）
     */
    public static <T> R<T> fail() {
        return fail(ResultCode.FAIL.getMessage());
    }

    /**
     * 失败返回（自定义消息）
     */
    public static <T> R<T> fail(String message) {
        return fail(ResultCode.FAIL.getCode(), message);
    }

    /**
     * 失败返回（自定义状态码和消息）
     */
    public static <T> R<T> fail(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    /**
     * 失败返回（使用ResultCode）
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
