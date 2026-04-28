package com.ddd.mall.domain.menu;

import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.ReconstructionOnly;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 菜单聚合根（树形结构，通过 parentId 自关联）
 */
@Getter
@ReconstructionOnly
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends AggregateRoot {

    /**
     * 菜单名称
     */
    @Setter
    private String name;

    /**
     * 父菜单ID
     */
    @Setter
    private Long parentId;

    /**
     * 路由路径
     */
    @Setter
    private String path;

    /**
     * 前端组件路径
     */
    @Setter
    private String component;

    /**
     * 菜单图标
     */
    @Setter
    private String icon;

    /**
     * 关联权限编码
     */
    @Setter
    private String permissionCode;

    /**
     * 菜单类型
     */
    @Setter
    private MenuType type;

    /**
     * 排序号
     */
    @Setter
    private Integer sort;

    /**
     * 是否可见
     */
    @Setter
    private Boolean visible;

    /**
     * 创建时间
     */
    @Setter
    private LocalDateTime createdAt;

    public Menu(String name, Long parentId, String path, String component,
                String icon, String permissionCode, MenuType type, Integer sort) {
        if (name == null || name.isBlank()) throw new DomainException("菜单名称不能为空");
        this.name = name;
        this.parentId = parentId;
        this.path = path;
        this.component = component;
        this.icon = icon;
        this.permissionCode = permissionCode;
        this.type = type;
        this.sort = sort != null ? sort : 0;
        this.visible = true;
        this.createdAt = LocalDateTime.now();
    }

    public void updateInfo(String name, String path, String component,
                           String icon, String permissionCode, Integer sort) {
        if (name == null || name.isBlank()) throw new DomainException("菜单名称不能为空");
        this.name = name;
        this.path = path;
        this.component = component;
        this.icon = icon;
        this.permissionCode = permissionCode;
        this.sort = sort;
    }

    public void hide() { this.visible = false; }
    public void show() { this.visible = true; }

    public boolean isRootMenu() { return this.parentId == null || this.parentId == 0; }
}