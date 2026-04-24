package com.ddd.mall.infrastructure.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录注解
 * 标注在 Controller 方法或类上，表示需要登录才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
    /**
     * 允许的用户类型，默认所有类型
     */
    UserType[] value() default {};
}
