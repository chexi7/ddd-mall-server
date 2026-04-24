package com.ddd.mall.infrastructure.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从 Authorization header 解析 token，设置到 SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = extractToken(httpRequest);

        if (token != null) {
            LoginUser loginUser = jwtTokenProvider.parseToken(token);
            if (loginUser != null) {
                SecurityContext.setCurrentUser(loginUser);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
