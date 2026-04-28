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

    /**
     * 创建时间
     */
    private String createdAt;

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