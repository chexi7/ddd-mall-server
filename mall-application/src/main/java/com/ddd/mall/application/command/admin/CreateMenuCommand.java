package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateMenuCommand {
    private final String name;
    private final Long parentId;
    private final String path;
    private final String component;
    private final String icon;
    private final String permissionCode;
    private final String type;
    private final Integer sort;
}
