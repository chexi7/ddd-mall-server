package com.ddd.mall.application.query.admin;

import com.ddd.mall.application.query.admin.dto.MenuTreeDto;
import com.ddd.mall.domain.menu.Menu;
import com.ddd.mall.domain.menu.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单聚合查询服务（构建树形结构）。
 */
@Service
@RequiredArgsConstructor
public class MenuQueryService {

    private final MenuRepository menuRepository;

    /**
     * 查询全量菜单树。
     *
     * @return 菜单树列表
     */
    @Transactional(readOnly = true)
    public List<MenuTreeDto> menuTree() {
        List<Menu> allMenus = menuRepository.findAll();
        List<MenuTreeDto> dtos = allMenus.stream().map(this::toDto).collect(Collectors.toList());
        return buildTree(dtos);
    }

    /**
     * 查询指定权限码对应的菜单树（用于登录后返回用户有权限的菜单）。
     *
     * @param permissionCodes 权限码列表
     * @return 菜单树列表
     */
    @Transactional(readOnly = true)
    public List<MenuTreeDto> menuTreeByPermissionCodes(List<String> permissionCodes) {
        List<Menu> menus = menuRepository.findByPermissionCodes(permissionCodes);
        List<MenuTreeDto> dtos = menus.stream().map(this::toDto).collect(Collectors.toList());
        return buildTree(dtos);
    }

    private MenuTreeDto toDto(Menu menu) {
        return MenuTreeDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .parentId(menu.getParentId())
                .path(menu.getPath())
                .component(menu.getComponent())
                .icon(menu.getIcon())
                .permissionCode(menu.getPermissionCode())
                .type(menu.getType().name())
                .sort(menu.getSort())
                .orderNum(menu.getSort())
                .visible(menu.getVisible())
                .createdAt(menu.getCreatedAt() == null ? null : menu.getCreatedAt().toString())
                .children(new ArrayList<>())
                .build();
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