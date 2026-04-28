package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.menu.Menu;
import com.ddd.mall.domain.menu.MenuType;
import com.ddd.mall.infrastructure.persistence.dataobject.MenuDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;

public class MenuConverter {

    public static Menu toDomain(MenuDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("name", d.getName());
        fields.put("parentId", d.getParentId());
        fields.put("path", d.getPath());
        fields.put("component", d.getComponent());
        fields.put("icon", d.getIcon());
        fields.put("permissionCode", d.getPermissionCode());
        fields.put("type", MenuType.valueOf(d.getType()));
        fields.put("sort", d.getSort());
        fields.put("visible", d.getVisible());
        fields.put("createdAt", d.getCreatedAt());

        Menu menu = DomainObjectReconstructor.reconstruct(Menu.class, fields);
        menu.clearDomainEvents();
        return menu;
    }

    public static MenuDO toDO(Menu menu) {
        MenuDO d = new MenuDO();
        d.setId(menu.getId());
        d.setVersion(menu.getVersion());
        d.setName(menu.getName());
        d.setParentId(menu.getParentId());
        d.setPath(menu.getPath());
        d.setComponent(menu.getComponent());
        d.setIcon(menu.getIcon());
        d.setPermissionCode(menu.getPermissionCode());
        d.setType(menu.getType().name());
        d.setSort(menu.getSort());
        d.setVisible(menu.getVisible());
        d.setCreatedAt(menu.getCreatedAt());
        return d;
    }
}