package com.fast.agent.tools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI 工具注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiTool {

    /**
     * 工具名称
     */
    String name();

    /**
     * 工具描述
     */
    String description() default "";
}
