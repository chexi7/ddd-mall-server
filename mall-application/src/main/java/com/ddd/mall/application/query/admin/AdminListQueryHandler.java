package com.ddd.mall.application.query.admin;

import com.ddd.mall.application.query.admin.dto.AdminListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.admin.Role;
import com.ddd.mall.domain.admin.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员分页列表
 */
@Service
@RequiredArgsConstructor
public class AdminListQueryHandler {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public PageResult<AdminListItemDto> handle(int page, int size, String keyword) {
        List<Admin> all = adminRepository.findAllAdmins();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Admin> filtered = all.stream()
                .filter(a -> kw.isEmpty()
                        || a.getUsername().toLowerCase().contains(kw)
                        || (a.getRealName() != null && a.getRealName().toLowerCase().contains(kw)))
                .collect(Collectors.toList());

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int from = Math.min((safePage - 1) * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        List<Admin> slice = filtered.subList(from, to);

        Map<Long, Role> roleMap = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        List<AdminListItemDto> content = new ArrayList<>();
        for (Admin admin : slice) {
            content.add(toDto(admin, roleMap));
        }

        int totalPages = (int) Math.ceil((double) filtered.size() / safeSize);
        return new PageResult<>(content, filtered.size(), totalPages, safePage, safeSize);
    }

    private AdminListItemDto toDto(Admin admin, Map<Long, Role> roleMap) {
        AdminListItemDto dto = new AdminListItemDto();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setRealName(admin.getRealName());
        String created = admin.getCreatedAt() == null ? null : admin.getCreatedAt().toString();
        dto.setCreatedAt(created);
        dto.setUpdatedAt(created);

        for (Long roleId : admin.getRoleIds()) {
            Role role = roleMap.get(roleId);
            if (role == null) {
                continue;
            }
            AdminListItemDto.AdminRoleBriefDto r = new AdminListItemDto.AdminRoleBriefDto();
            r.setId(role.getId());
            r.setName(role.getName());
            r.setCode(role.getCode());
            dto.getRoles().add(r);
        }
        return dto;
    }
}
