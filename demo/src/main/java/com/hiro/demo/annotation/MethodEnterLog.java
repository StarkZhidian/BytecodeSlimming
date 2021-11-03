package com.hiro.demo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiuhongwei.hiro
 * @date 2021/11/3
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface MethodEnterLog {
}
