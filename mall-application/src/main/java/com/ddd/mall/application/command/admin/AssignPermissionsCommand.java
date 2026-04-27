package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AssignPermissionsCommand {
    private final Long roleId;
    private final List<String> permissionCodes;
}