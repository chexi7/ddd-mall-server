package com.ddd.mall.domain.admin;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 权限值对象（不可变）
 * 例如：code="product:create", name="创建商品", type=API
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Permission {
    private final String code;
    private final String name;
    private final PermissionType type;
}
