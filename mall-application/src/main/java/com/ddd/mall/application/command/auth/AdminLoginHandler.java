package com.ddd.mall.application.command.auth;

import com.ddd.mall.domain.shared.TokenService;
import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.admin.Role;
import com.ddd.mall.domain.admin.RoleRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminLoginHandler {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;

    public LoginResult handle(AdminLoginCommand command) {
        Admin admin = adminRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new DomainException("用户名或密码错误"));

        if (!admin.verifyPassword(command.getPassword())) {
            throw new DomainException("用户名或密码错误");
        }
        if (!admin.isEnabled()) {
            throw new DomainException("账号已被停用");
        }

        List<Role> roles = roleRepository.findByIds(admin.getRoleIds());
        List<String> roleNames = roles.stream().map(Role::getCode).collect(Collectors.toList());
        Set<String> permissions = new HashSet<>();
        for (Role role : roles) {
            permissions.addAll(role.getPermissionCodes());
        }

        String token = tokenService.generateAdminToken(admin.getId(), admin.getUsername(), roleNames, permissions);
        return new LoginResult(token, admin.getId(), admin.getUsername(), admin.getRealName(), roleNames, permissions);
    }

    @Getter
    @RequiredArgsConstructor
    public static class LoginResult {
        private final String token;
        private final Long userId;
        private final String username;
        private final String realName;
        private final List<String> roles;
        private final Set<String> permissions;
    }
}
