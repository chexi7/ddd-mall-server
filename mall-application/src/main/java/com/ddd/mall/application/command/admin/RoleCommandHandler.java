package com.ddd.mall.application.command.admin;

import com.ddd.mall.domain.role.Role;
import com.ddd.mall.domain.role.RoleRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色聚合命令处理器，处理角色相关命令。
 * <p>
 * 对应 DDD 铁律：一个命令处理器只操作一个聚合，聚合仅可以在命令处理器中进行操作。
 */
@Service
@RequiredArgsConstructor
public class RoleCommandHandler {

    private final RoleRepository roleRepository;

    /**
     * 处理创建角色命令。
     */
    @Transactional
    public Long handle(CreateRoleCommand command) {
        if (roleRepository.existsByCode(command.getCode())) {
            throw new DomainException("角色编码已存在: " + command.getCode());
        }
        Role role = new Role(command.getName(), command.getCode(), command.getDescription());
        roleRepository.save(role);
        return role.getId();
    }

    /**
     * 处理分配角色权限命令。
     */
    @Transactional
    public void handle(AssignPermissionsCommand command) {
        Role role = roleRepository.findById(command.getRoleId())
                .orElseThrow(() -> new DomainException("角色不存在: " + command.getRoleId()));
        role.assignPermissions(command.getPermissionCodes());
        roleRepository.save(role);
    }
}