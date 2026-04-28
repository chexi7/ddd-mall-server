package com.ddd.mall.application.command.admin;

import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员聚合应用服务，承接管理员相关业务用例。
 */
@Service
@RequiredArgsConstructor
public class AdminApplicationService {

    /**
     * 管理员仓储
     */
    private final AdminRepository adminRepository;

    /**
     * 创建管理员。
     *
     * @param command 创建管理员命令
     * @return 新建管理员 ID
     */
    @Transactional
    public Long createAdmin(CreateAdminCommand command) {
        if (adminRepository.existsByUsername(command.getUsername())) {
            throw new DomainException("用户名已存在: " + command.getUsername());
        }

        Admin admin = new Admin(command.getUsername(), command.getPassword(), command.getRealName());
        admin.updateContactInfo(command.getPhone(), command.getEmail());

        adminRepository.save(admin);
        return admin.getId();
    }

    /**
     * 给指定管理员分配角色。
     *
     * @param command 分配角色命令
     */
    @Transactional
    public void assignRoles(AssignRolesCommand command) {
        Admin admin = adminRepository.findById(command.getAdminId())
                .orElseThrow(() -> new DomainException("管理员不存在: " + command.getAdminId()));
        admin.assignRoles(command.getRoleIds());
        adminRepository.save(admin);
    }
}