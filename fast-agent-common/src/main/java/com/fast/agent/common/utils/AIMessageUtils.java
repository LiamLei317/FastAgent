package com.fast.agent.common.utils;


public class AIMessageUtils {

    /**
     * todo 手动拼接消息，不够规范，待淘汰
     */
    public static String buildFullMessage(String skillPrompt, String userQuestion) {

        return "系统指令：" + skillPrompt + "\n\n" +
                "用户问题：" + userQuestion;

    }
}
