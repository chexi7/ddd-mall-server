package com.ddd.mall.application.command.admin;

import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignRolesHandler {

    private final AdminRepository adminRepository;

    @Transactional
    public void handle(AssignRolesCommand command) {
        Admin admin = adminRepository.findById(command.getAdminId())
                .orElseThrow(() -> new DomainException("管理员不存在: " + command.getAdminId()));
        admin.assignRoles(command.getRoleIds());
        adminRepository.save(admin);
    }
}
