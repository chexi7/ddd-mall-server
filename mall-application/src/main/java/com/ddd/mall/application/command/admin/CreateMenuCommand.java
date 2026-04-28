package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateMenuCommand {

    /**
     * 菜单名称
     */
    private final String name;

    /**
     * 父菜单ID
     */
    private final Long parentId;

    /**
     * 路由路径
     */
    private final String path;

    /**
     * 前端组件路径
     */
    private final String component;

    /**
     * 菜单图标
     */
    private final String icon;

    /**
     * 关联权限编码
     */
    private final String permissionCode;

    /**
     * 菜单类型
     */
    private final String type;

    /**
     * 排序号
     */
    private final Integer sort;
}