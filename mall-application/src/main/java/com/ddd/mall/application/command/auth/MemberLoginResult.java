package com.ddd.mall.application.command.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 会员登录结果。
 */
@Getter
@RequiredArgsConstructor
public class MemberLoginResult {
    private final String token;
    private final Long userId;
    private final String username;
    private final String nickname;
}
