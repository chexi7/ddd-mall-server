package com.ddd.mall.application.command.admin.handler;

import com.ddd.mall.application.command.admin.cmd.CreateRoleCommand;
import com.ddd.mall.domain.admin.Role;
import com.ddd.mall.domain.admin.RoleRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRoleHandler {

    private final RoleRepository roleRepository;

    @Transactional
    public Long handle(CreateRoleCommand command) {
        if (roleRepository.existsByCode(command.getCode())) {
            throw new DomainException("角色编码已存在: " + command.getCode());
        }
        Role role = new Role(command.getName(), command.getCode(), command.getDescription());
        roleRepository.save(role);
        return role.getId();
    }
}