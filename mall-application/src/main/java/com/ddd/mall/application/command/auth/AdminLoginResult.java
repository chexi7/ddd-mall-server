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

    /**
     * 认证令牌
     */
    private final String token;

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 真实姓名
     */
    private final String realName;

    /**
     * 角色列表
     */
    private final List<String> roles;

    /**
     * 权限集合
     */
    private final Set<String> permissions;
}