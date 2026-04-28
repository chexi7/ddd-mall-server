package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AssignPermissionsCommand {

    /**
     * 角色ID
     */
    private final Long roleId;

    /**
     * 权限编码列表
     */
    private final List<String> permissionCodes;
}