package com.ddd.mall.application.command.admin.handler;

import com.ddd.mall.application.command.admin.cmd.AssignPermissionsCommand;
import com.ddd.mall.domain.admin.Role;
import com.ddd.mall.domain.admin.RoleRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignPermissionsHandler {

    private final RoleRepository roleRepository;

    @Transactional
    public void handle(AssignPermissionsCommand command) {
        Role role = roleRepository.findById(command.getRoleId())
                .orElseThrow(() -> new DomainException("角色不存在: " + command.getRoleId()));
        role.assignPermissions(command.getPermissionCodes());
        roleRepository.save(role);
    }
}