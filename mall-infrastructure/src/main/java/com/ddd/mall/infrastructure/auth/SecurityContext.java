package com.ddd.mall.infrastructure.auth;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 */
public class SecurityContext {

    private static final ThreadLocal<LoginUser> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(LoginUser user) {
        CURRENT_USER.set(user);
    }

    public static LoginUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }

    public static Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
