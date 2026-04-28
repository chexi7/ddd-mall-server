package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.role.Role;
import com.ddd.mall.domain.role.RoleRepository;
import com.ddd.mall.infrastructure.persistence.RoleJpaRepository;
import com.ddd.mall.infrastructure.persistence.RolePermissionJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.RoleConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.RoleDO;
import com.ddd.mall.infrastructure.persistence.dataobject.RolePermissionDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final RolePermissionJpaRepository rolePermissionJpaRepository;

    @Override
    public Optional<Role> findById(Long id) {
        return roleJpaRepository.findById(id).map(this::toDomainWithPermissions);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return roleJpaRepository.findByCode(code).map(this::toDomainWithPermissions);
    }

    @Override
    public List<Role> findByIds(List<Long> ids) {
        return roleJpaRepository.findByIdIn(ids).stream()
                .map(this::toDomainWithPermissions)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll().stream()
                .map(this::toDomainWithPermissions)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return roleJpaRepository.existsByCode(code);
    }

    @Override
    @Transactional
    public void save(Role role) {
        RoleDO saved = roleJpaRepository.save(RoleConverter.toDO(role));
        DomainObjectReconstructor.setIdAndVersion(role, saved.getId(), saved.getVersion());

        // 保存权限关联（全量替换）
        rolePermissionJpaRepository.deleteByRoleId(saved.getId());
        for (String permissionCode : role.getPermissionCodes()) {
            RolePermissionDO rp = new RolePermissionDO();
            rp.setRoleId(saved.getId());
            rp.setPermissionCode(permissionCode);
            rolePermissionJpaRepository.save(rp);
        }
    }

    private Role toDomainWithPermissions(RoleDO roleDO) {
        Role role = RoleConverter.toDomain(roleDO);
        List<RolePermissionDO> permissions = rolePermissionJpaRepository.findByRoleId(roleDO.getId());
        for (RolePermissionDO rp : permissions) {
            role.addPermissionCodeInternal(rp.getPermissionCode());
        }
        return role;
    }
}
