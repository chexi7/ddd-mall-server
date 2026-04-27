package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.admin.AdminRepository;
import com.ddd.mall.infrastructure.persistence.AdminJpaRepository;
import com.ddd.mall.infrastructure.persistence.AdminRoleJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.AdminConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.AdminDO;
import com.ddd.mall.infrastructure.persistence.dataobject.AdminRoleDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminJpaRepository adminJpaRepository;
    private final AdminRoleJpaRepository adminRoleJpaRepository;

    @Override
    public Optional<Admin> findById(Long id) {
        return adminJpaRepository.findById(id).map(this::toDomainWithRoles);
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return adminJpaRepository.findByUsername(username).map(this::toDomainWithRoles);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminJpaRepository.existsByUsername(username);
    }

    @Override
    public List<Admin> findAllAdmins() {
        return adminJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toDomainWithRoles)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(Admin admin) {
        AdminDO saved = adminJpaRepository.save(AdminConverter.toDO(admin));
        admin.setId(saved.getId());
        admin.setVersion(saved.getVersion());

        // 保存角色关联（全量替换）
        adminRoleJpaRepository.deleteByAdminId(saved.getId());
        for (Long roleId : admin.getRoleIds()) {
            AdminRoleDO ar = new AdminRoleDO();
            ar.setAdminId(saved.getId());
            ar.setRoleId(roleId);
            adminRoleJpaRepository.save(ar);
        }
    }

    private Admin toDomainWithRoles(AdminDO adminDO) {
        Admin admin = AdminConverter.toDomain(adminDO);
        List<AdminRoleDO> roles = adminRoleJpaRepository.findByAdminId(adminDO.getId());
        for (AdminRoleDO ar : roles) {
            admin.addRoleIdInternal(ar.getRoleId());
        }
        return admin;
    }
}
