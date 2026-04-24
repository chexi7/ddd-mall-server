package com.ddd.mall.application.query.admin;

import com.ddd.mall.application.query.admin.dto.MenuTreeDto;
import com.ddd.mall.domain.admin.Menu;
import com.ddd.mall.domain.admin.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单树查询（构建树形结构）
 */
@Service
@RequiredArgsConstructor
public class MenuTreeQueryHandler {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<MenuTreeDto> handle() {
        List<Menu> allMenus = menuRepository.findAll();
        List<MenuTreeDto> dtos = allMenus.stream().map(this::toDto).collect(Collectors.toList());
        return buildTree(dtos);
    }

    /**
     * 查询指定权限码对应的菜单树（用于登录后返回用户有权限的菜单）
     */
    @Transactional(readOnly = true)
    public List<MenuTreeDto> handleByPermissionCodes(List<String> permissionCodes) {
        List<Menu> menus = menuRepository.findByPermissionCodes(permissionCodes);
        List<MenuTreeDto> dtos = menus.stream().map(this::toDto).collect(Collectors.toList());
        return buildTree(dtos);
    }

    private MenuTreeDto toDto(Menu menu) {
        MenuTreeDto dto = new MenuTreeDto();
        dto.setId(menu.getId());
        dto.setName(menu.getName());
        dto.setParentId(menu.getParentId());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setIcon(menu.getIcon());
        dto.setPermissionCode(menu.getPermissionCode());
        dto.setType(menu.getType().name());
        dto.setSort(menu.getSort());
        dto.setVisible(menu.getVisible());
        return dto;
    }

    private List<MenuTreeDto> buildTree(List<MenuTreeDto> dtos) {
        Map<Long, MenuTreeDto> map = dtos.stream()
                .collect(Collectors.toMap(MenuTreeDto::getId, d -> d));

        List<MenuTreeDto> roots = new ArrayList<>();
        for (MenuTreeDto dto : dtos) {
            if (dto.getParentId() == null || dto.getParentId() == 0) {
                roots.add(dto);
            } else {
                MenuTreeDto parent = map.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }
}
