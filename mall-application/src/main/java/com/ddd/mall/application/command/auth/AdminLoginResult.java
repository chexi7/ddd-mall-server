package com.ddd.mall.application.command.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 管理员登录结果。
 */
@Getter
@RequiredArgsConstructor
public class AdminLoginResult {
    private final String token;
    private final Long userId;
    private final String username;
    private final String realName;
    private final List<String> roles;
    private final Set<String> permissions;
}
