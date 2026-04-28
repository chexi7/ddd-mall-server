package com.ddd.mall.application.query.admin;

import com.ddd.mall.application.query.admin.dto.AdminListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.role.Role;
import com.ddd.mall.domain.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员聚合查询服务。
 */
@Service
@RequiredArgsConstructor
public class AdminQueryService {

    /**
     * 管理员仓储
     */
    private final AdminRepository adminRepository;

    /**
     * 角色仓储
     */
    private final RoleRepository roleRepository;

    /**
     * 管理员分页列表。
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 搜索关键字
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<AdminListItemDto> adminList(int page, int size, String keyword) {
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
        return PageResult.<AdminListItemDto>builder()
                .data(content)
                .totalCount(filtered.size())
                .totalPages(totalPages)
                .pageNum(safePage)
                .pageSize(safeSize)
                .build();
    }

    private AdminListItemDto toDto(Admin admin, Map<Long, Role> roleMap) {
        List<AdminListItemDto.AdminRoleBriefDto> roleDtos = new ArrayList<>();
        for (Long roleId : admin.getRoleIds()) {
            Role role = roleMap.get(roleId);
            if (role == null) {
                continue;
            }
            List<AdminListItemDto.RolePermissionBriefDto> permDtos = role.getPermissionCodes().stream()
                    .map(code -> AdminListItemDto.RolePermissionBriefDto.builder()
                            .id((long) Math.abs(code.hashCode()))
                            .name(code)
                            .code(code)
                            .build())
                    .collect(Collectors.toList());

            roleDtos.add(AdminListItemDto.AdminRoleBriefDto.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .code(role.getCode())
                    .permissions(permDtos)
                    .build());
        }

        String created = admin.getCreatedAt() == null ? null : admin.getCreatedAt().toString();
        return AdminListItemDto.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .roles(roleDtos)
                .createdAt(created)
                .updatedAt(created)
                .build();
    }
}