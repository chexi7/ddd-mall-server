package com.ddd.mall.application.command.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 会员登录结果。
 */
@Getter
@RequiredArgsConstructor
public class MemberLoginResult {

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
     * 昵称
     */
    private final String nickname;
}