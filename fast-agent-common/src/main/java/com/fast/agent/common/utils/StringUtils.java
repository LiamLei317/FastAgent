package com.fast.agent.common.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类
 */
public class StringUtils {

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 判断字符串是否为空白（包括null、空字符串、纯空白字符）
     */
    public static boolean isBlank(String str) {
        return StrUtil.isBlank(str);
    }

    /**
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return StrUtil.isNotBlank(str);
    }

    /**
     * 字符串截取
     */
    public static String substring(String str, int start, int end) {
        return StrUtil.sub(str, start, end);
    }

    /**
     * 去除字符串两端的空白字符
     */
    public static String trim(String str) {
        return StrUtil.trim(str);
    }

    private StringUtils() {
    }
}
