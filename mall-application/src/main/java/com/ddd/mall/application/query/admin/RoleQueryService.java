package com.ddd.mall.application.query.admin;

import com.ddd.mall.application.query.admin.dto.RoleListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.role.Role;
import com.ddd.mall.domain.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色聚合查询服务。
 */
@Service
@RequiredArgsConstructor
public class RoleQueryService {

    /**
     * 角色仓储
     */
    private final RoleRepository roleRepository;

    /**
     * 角色分页列表。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<RoleListItemDto> roleList(int page, int size) {
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
        return PageResult.<RoleListItemDto>builder()
                .data(content)
                .totalCount(all.size())
                .totalPages(totalPages)
                .pageNum(safePage)
                .pageSize(safeSize)
                .build();
    }

    private RoleListItemDto toDto(Role role) {
        List<RoleListItemDto.RolePermissionBriefDto> permDtos = role.getPermissionCodes().stream()
                .map(code -> RoleListItemDto.RolePermissionBriefDto.builder()
                        .id((long) Math.abs(code.hashCode()))
                        .name(code)
                        .code(code)
                        .build())
                .collect(Collectors.toList());

        return RoleListItemDto.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .permissions(permDtos)
                .createdAt(role.getCreatedAt() == null ? null : role.getCreatedAt().toString())
                .build();
    }
}