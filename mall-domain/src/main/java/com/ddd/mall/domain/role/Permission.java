package com.ddd.mall.domain.role;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 权限值对象（不可变）
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Permission {

    /**
     * 权限编码
     */
    private final String code;

    /**
     * 权限名称
     */
    private final String name;

    /**
     * 权限类型
     */
    private final PermissionType type;
}