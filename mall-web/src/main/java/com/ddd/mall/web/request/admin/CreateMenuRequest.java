package com.ddd.mall.web.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建菜单请求参数
 */
@Getter
@Setter
public class CreateMenuRequest {
    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 父级菜单ID 根菜单可为空
     */
    private Long parentId;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 菜单类型
     */
    @NotNull(message = "菜单类型不能为空")
    private String type;

    /**
     * 排序值
     */
    private Integer sort;
}
