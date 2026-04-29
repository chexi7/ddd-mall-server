package com.ddd.mall.application.command.auth;

import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.role.Role;
import com.ddd.mall.domain.role.RoleRepository;
import com.ddd.mall.domain.member.Member;
import com.ddd.mall.domain.member.MemberRepository;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.application.command.auth.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证命令处理器，处理管理端与会员端的登录命令。
 */
@Service
@RequiredArgsConstructor
public class AuthCommandHandler {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final TokenService tokenService;

    /**
     * 处理管理员登录命令。
     */
    public AdminLoginResult handle(AdminLoginCommand command) {
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
        return new AdminLoginResult(token, admin.getId(), admin.getUsername(), admin.getRealName(), roleNames, permissions);
    }

    /**
     * 处理会员登录命令。
     */
    public MemberLoginResult handle(MemberLoginCommand command) {
        Member member = memberRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new DomainException("用户名或密码错误"));

        if (!member.getPassword().equals(command.getPassword())) {
            throw new DomainException("用户名或密码错误");
        }

        String token = tokenService.generateMemberToken(member.getId(), member.getUsername());
        return new MemberLoginResult(token, member.getId(), member.getUsername(), member.getNickname());
    }
}