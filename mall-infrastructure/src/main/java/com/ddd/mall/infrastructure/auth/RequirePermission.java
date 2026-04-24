package com.ddd.mall.infrastructure.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限注解
 * 标注在 Controller 方法上，表示需要指定权限才能访问
 *
 * 示例：@RequirePermission("product:create")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * 权限标识，如 "product:create"
     */
    String value();
}
