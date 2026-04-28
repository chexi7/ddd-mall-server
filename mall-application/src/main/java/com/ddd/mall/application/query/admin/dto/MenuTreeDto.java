package com.ddd.mall.application.query.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理端菜单树节点
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeDto {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 路由路径
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
     * 关联权限编码
     */
    private String permissionCode;

    /**
     * 菜单类型
     */
    private String type;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 与前端字段 orderNum 对齐，取值同 sort
     */
    private Integer orderNum;

    /**
     * 是否可见
     */
    private Boolean visible;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 子菜单列表
     */
    private List<MenuTreeDto> children;
}