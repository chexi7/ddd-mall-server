package com.ddd.mall.application.command.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AdminLoginCommand {

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    private final String password;
}