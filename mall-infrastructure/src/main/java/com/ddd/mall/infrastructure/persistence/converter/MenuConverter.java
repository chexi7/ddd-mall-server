package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.admin.Menu;
import com.ddd.mall.domain.admin.MenuType;
import com.ddd.mall.infrastructure.persistence.dataobject.MenuDO;

public class MenuConverter {

    public static Menu toDomain(MenuDO d) {
        Menu menu = new Menu(d.getName(), d.getParentId(), d.getPath(), d.getComponent(),
                d.getIcon(), d.getPermissionCode(), MenuType.valueOf(d.getType()), d.getSort());
        menu.setId(d.getId());
        menu.setVersion(d.getVersion());
        menu.setVisible(d.getVisible());
        menu.setCreatedAt(d.getCreatedAt());
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
