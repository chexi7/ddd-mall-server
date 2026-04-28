package com.ddd.mall.infrastructure.auth;

import com.ddd.mall.application.command.auth.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * JWT 实现的 TokenService
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String generateAdminToken(Long userId, String username, List<String> roles, Set<String> permissions) {
        LoginUser loginUser = new LoginUser(userId, username, UserType.ADMIN, roles, permissions);
        return jwtTokenProvider.generateToken(loginUser);
    }

    @Override
    public String generateMemberToken(Long userId, String username) {
        LoginUser loginUser = new LoginUser(userId, username, UserType.MEMBER,
                Collections.emptyList(), Collections.emptySet());
        return jwtTokenProvider.generateToken(loginUser);
    }
}
