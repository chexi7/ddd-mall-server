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
 * 认证相关应用服务，承接管理端与会员端的登录用例。
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    /**
     * 管理员仓储
     */
    private final AdminRepository adminRepository;

    /**
     * 角色仓储
     */
    private final RoleRepository roleRepository;

    /**
     * 会员仓储
     */
    private final MemberRepository memberRepository;

    /**
     * Token服务
     */
    private final TokenService tokenService;

    /**
     * 管理员登录。
     *
     * @param command 管理员登录命令
     * @return 管理员登录结果
     */
    public AdminLoginResult adminLogin(AdminLoginCommand command) {
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
     * 会员登录。
     *
     * @param command 会员登录命令
     * @return 会员登录结果
     */
    public MemberLoginResult memberLogin(MemberLoginCommand command) {
        Member member = memberRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new DomainException("用户名或密码错误"));

        if (!member.getPassword().equals(command.getPassword())) {
            throw new DomainException("用户名或密码错误");
        }

        String token = tokenService.generateMemberToken(member.getId(), member.getUsername());
        return new MemberLoginResult(token, member.getId(), member.getUsername(), member.getNickname());
    }
}