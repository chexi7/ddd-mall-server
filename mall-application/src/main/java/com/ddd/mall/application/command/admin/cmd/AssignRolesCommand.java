package com.ddd.mall.application.command.admin.cmd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AssignRolesCommand {
    private final Long adminId;
    private final List<Long> roleIds;
}