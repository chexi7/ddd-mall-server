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

    /**
     * 管理员ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色列表
     */
    private List<AdminRoleBriefDto> roles;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminRoleBriefDto {

        /**
         * 角色ID
         */
        private Long id;

        /**
         * 角色名称
         */
        private String name;

        /**
         * 角色编码
         */
        private String code;

        /**
         * 权限列表
         */
        private List<RolePermissionBriefDto> permissions;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolePermissionBriefDto {

        /**
         * 权限ID
         */
        private Long id;

        /**
         * 权限名称
         */
        private String name;

        /**
         * 权限编码
         */
        private String code;
    }
}