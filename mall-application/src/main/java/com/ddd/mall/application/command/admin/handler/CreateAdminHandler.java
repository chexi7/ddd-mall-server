package com.ddd.mall.application.command.admin.handler;

import com.ddd.mall.application.command.admin.cmd.CreateAdminCommand;
import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAdminHandler {

    private final AdminRepository adminRepository;

    @Transactional
    public Long handle(CreateAdminCommand command) {
        if (adminRepository.existsByUsername(command.getUsername())) {
            throw new DomainException("用户名已存在: " + command.getUsername());
        }

        Admin admin = new Admin(command.getUsername(), command.getPassword(), command.getRealName());
        admin.setPhone(command.getPhone());
        admin.setEmail(command.getEmail());

        adminRepository.save(admin);
        return admin.getId();
    }
}