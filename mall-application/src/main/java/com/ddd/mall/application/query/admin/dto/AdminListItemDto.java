package com.ddd.mall.application.query.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端管理员列表项
 */
@Getter
@Setter
public class AdminListItemDto {
    private Long id;
    private String username;
    private String realName;
    private List<AdminRoleBriefDto> roles = new ArrayList<>();
    private String createdAt;
    private String updatedAt;

    @Getter
    @Setter
    public static class AdminRoleBriefDto {
        private Long id;
        private String name;
        private String code;
        private List<RolePermissionBriefDto> permissions = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class RolePermissionBriefDto {
        private Integer id;
        private String name;
        private String code;
    }
}
