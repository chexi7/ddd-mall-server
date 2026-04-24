package com.ddd.mall.domain.admin;

import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 菜单聚合根（树形结构，通过 parentId 自关联）
 */
@Getter
public class Menu extends AggregateRoot {

    @Setter private String name;
    @Setter private Long parentId;
    @Setter private String path;
    @Setter private String component;
    @Setter private String icon;
    @Setter private String permissionCode;
    @Setter private MenuType type;
    @Setter private Integer sort;
    @Setter private Boolean visible;
    @Setter private LocalDateTime createdAt;

    protected Menu() {}

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
