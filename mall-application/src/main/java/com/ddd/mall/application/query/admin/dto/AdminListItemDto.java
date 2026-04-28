package com.ddd.mall.application.query.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理端管理员列表项
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListItemDto {
    private Long id;
    private String username;
    private String realName;
    private List<AdminRoleBriefDto> roles;
    private String createdAt;
    private String updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminRoleBriefDto {
        private Long id;
        private String name;
        private String code;
        private List<RolePermissionBriefDto> permissions;
    }

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