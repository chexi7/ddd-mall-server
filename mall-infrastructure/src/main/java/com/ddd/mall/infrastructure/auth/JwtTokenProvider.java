package com.ddd.mall.infrastructure.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT Token 签发与解析
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${mall.jwt.secret:mall-ddd-secret-key-must-be-at-least-32-bytes}") String secret,
            @Value("${mall.jwt.expiration-ms:86400000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 生成 Token
     */
    public String generateToken(LoginUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("username", user.getUsername())
                .claim("userType", user.getUserType().name())
                .claim("roles", user.getRoles())
                .claim("permissions", new ArrayList<>(user.getPermissions()))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 Token，返回 LoginUser；无效则返回 null
     */
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);
            UserType userType = UserType.valueOf(claims.get("userType", String.class));
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            @SuppressWarnings("unchecked")
            List<String> permissionList = claims.get("permissions", List.class);
            Set<String> permissions = permissionList != null ? new HashSet<>(permissionList) : Collections.emptySet();

            return new LoginUser(userId, username, userType, roles, permissions);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
