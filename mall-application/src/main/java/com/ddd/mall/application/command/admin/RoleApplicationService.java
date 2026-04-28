package com.ddd.mall.application.command.admin;

import com.ddd.mall.domain.role.Role;
import com.ddd.mall.domain.role.RoleRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色聚合应用服务，承接角色相关业务用例。
 */
@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleRepository roleRepository;

    /**
     * 创建角色。
     *
     * @param command 创建角色命令
     * @return 新建角色 ID
     */
    @Transactional
    public Long createRole(CreateRoleCommand command) {
        if (roleRepository.existsByCode(command.getCode())) {
            throw new DomainException("角色编码已存在: " + command.getCode());
        }
        Role role = new Role(command.getName(), command.getCode(), command.getDescription());
        roleRepository.save(role);
        return role.getId();
    }

    /**
     * 给指定角色分配权限。
     *
     * @param command 分配权限命令
     */
    @Transactional
    public void assignPermissions(AssignPermissionsCommand command) {
        Role role = roleRepository.findById(command.getRoleId())
                .orElseThrow(() -> new DomainException("角色不存在: " + command.getRoleId()));
        role.assignPermissions(command.getPermissionCodes());
        roleRepository.save(role);
    }
}
