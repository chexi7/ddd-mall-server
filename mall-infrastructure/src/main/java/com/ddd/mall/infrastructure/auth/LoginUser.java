package com.ddd.mall.infrastructure.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 当前登录用户信息（JWT 中携带的信息 + 查出的权限）
 */
@Getter
@RequiredArgsConstructor
public class LoginUser {
    private final Long userId;
    private final String username;
    private final UserType userType;
    private final List<String> roles;
    private final Set<String> permissions;

    public boolean hasPermission(String permissionCode) {
        return permissions != null && permissions.contains(permissionCode);
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }
}
