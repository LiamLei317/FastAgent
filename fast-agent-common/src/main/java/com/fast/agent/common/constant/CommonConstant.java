package com.fast.agent.common.constant;

/**
 * 全局公用常量
 */
public class CommonConstant {

    /**
     * UTF-8 编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 分隔符
     */
    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";
    public static final String COLON = ":";
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String UNDERLINE = "_";
    public static final String HYPHEN = "-";

    /**
     * 默认值
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer DEFAULT_PAGE_NUM = 1;
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 状态常量
     */
    public static final Integer STATUS_ENABLE = 1;
    public static final Integer STATUS_DISABLE = 0;
    public static final Integer STATUS_DELETED = 2;

    /**
     * Redis Key 前缀
     */
    public static final String REDIS_KEY_PREFIX = "fast:agent:";

    private CommonConstant() {
    }
}
