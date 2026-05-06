package com.fast.agent.common.utils;

import cn.hutool.core.date.DateUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtils {

    /**
     * 默认日期时间格式
     */
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 格式化日期时间为字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }

    /**
     * 格式化日期为字符串
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DateUtil.format(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 格式化日期时间为字符串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return DateUtil.format(date, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 获取当前时间戳（秒）
     */
    public static long currentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    public static long currentTimestampMillis() {
        return System.currentTimeMillis();
    }

    private DateUtils() {
    }
}
