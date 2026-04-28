package com.ddd.mall.application.query.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理端角色列表项
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleListItemDto {
    private Long id;
    private String name;
    private String code;
    private List<RolePermissionBriefDto> permissions;
    private String createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolePermissionBriefDto {
        private Long id;
        private String name;
        private String code;
    }
}