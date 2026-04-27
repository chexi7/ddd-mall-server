package com.ddd.mall.application.query.admin;

import com.ddd.mall.application.query.admin.dto.RoleListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.admin.Role;
import com.ddd.mall.domain.admin.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色分页列表
 */
@Service
@RequiredArgsConstructor
public class RoleListQueryHandler {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public PageResult<RoleListItemDto> handle(int page, int size) {
        List<Role> all = roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getId))
                .collect(Collectors.toList());

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        List<RoleListItemDto> content = new ArrayList<>();
        for (Role role : all.subList(from, to)) {
            content.add(toDto(role));
        }
        int totalPages = (int) Math.ceil((double) all.size() / safeSize);
        return new PageResult<>(content, all.size(), totalPages, safePage, safeSize);
    }

    private RoleListItemDto toDto(Role role) {
        RoleListItemDto dto = new RoleListItemDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setCreatedAt(role.getCreatedAt() == null ? null : role.getCreatedAt().toString());
        for (String code : role.getPermissionCodes()) {
            RoleListItemDto.RolePermissionBriefDto p = new RoleListItemDto.RolePermissionBriefDto();
            p.setId(Math.abs(code.hashCode()));
            p.setName(code);
            p.setCode(code);
            dto.getPermissions().add(p);
        }
        return dto;
    }
}
