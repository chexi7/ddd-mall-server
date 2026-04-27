package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateAdminCommand {
    private final String username;
    private final String password;
    private final String realName;
    private final String phone;
    private final String email;
}