package com.ddd.mall.application.query.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端角色列表项
 */
@Getter
@Setter
public class RoleListItemDto {
    private Long id;
    private String name;
    private String code;
    private List<RolePermissionBriefDto> permissions = new ArrayList<>();
    private String createdAt;

    @Getter
    @Setter
    public static class RolePermissionBriefDto {
        private Integer id;
        private String name;
        private String code;
    }
}
