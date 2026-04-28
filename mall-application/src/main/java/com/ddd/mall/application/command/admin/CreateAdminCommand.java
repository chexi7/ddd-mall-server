package com.ddd.mall.application.command.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateAdminCommand {

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    private final String password;

    /**
     * 真实姓名
     */
    private final String realName;

    /**
     * 手机号
     */
    private final String phone;

    /**
     * 邮箱
     */
    private final String email;
}