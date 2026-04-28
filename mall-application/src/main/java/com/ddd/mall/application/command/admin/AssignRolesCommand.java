package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AssignRolesCommand {

    /**
     * 管理员ID
     */
    private final Long adminId;

    /**
     * 角色ID列表
     */
    private final List<Long> roleIds;
}