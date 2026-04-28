package com.ddd.mall.domain.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记聚合根/实体的 protected 无参构造函数仅供仓储重建使用。
 * <p>
 * 业务代码禁止通过此构造函数创建对象，必须使用包含业务校验的公有构造函数或工厂方法。
 * 此注解与 Lombok {@code @NoArgsConstructor(access = AccessLevel.PROTECTED)} 配合使用，
 * 由 Lombok 自动生成 protected 无参构造函数，本注解仅声明设计意图。
 * <p>
 * 示例：
 * <pre>
 * &#64;ReconstructionOnly
 * &#64;NoArgsConstructor(access = AccessLevel.PROTECTED)
 * public class Order extends AggregateRoot { ... }
 * </pre>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ReconstructionOnly {
}