package com.ddd.mall.application.command.admin;

import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员聚合命令处理器，处理管理员相关命令。
 * <p>
 * 对应 DDD 铁律：一个命令处理器只操作一个聚合，聚合仅可以在命令处理器中进行操作。
 */
@Service
@RequiredArgsConstructor
public class AdminCommandHandler {

    private final AdminRepository adminRepository;

    /**
     * 处理创建管理员命令。
     */
    @Transactional
    public Long handle(CreateAdminCommand command) {
        if (adminRepository.existsByUsername(command.getUsername())) {
            throw new DomainException("用户名已存在: " + command.getUsername());
        }

        Admin admin = new Admin(command.getUsername(), command.getPassword(), command.getRealName());
        admin.updateContactInfo(command.getPhone(), command.getEmail());

        adminRepository.save(admin);
        return admin.getId();
    }

    /**
     * 处理分配管理员角色命令。
     */
    @Transactional
    public void handle(AssignRolesCommand command) {
        Admin admin = adminRepository.findById(command.getAdminId())
                .orElseThrow(() -> new DomainException("管理员不存在: " + command.getAdminId()));
        admin.assignRoles(command.getRoleIds());
        adminRepository.save(admin);
    }
}