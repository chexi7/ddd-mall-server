package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateRoleCommand {

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 角色编码
     */
    private final String code;

    /**
     * 角色描述
     */
    private final String description;
}