package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.shared.CommonStatus;
import com.ddd.mall.domain.role.Role;
import com.ddd.mall.infrastructure.persistence.dataobject.RoleDO;

public class RoleConverter {

    public static Role toDomain(RoleDO d) {
        Role role = new Role(d.getName(), d.getCode(), d.getDescription());
        role.setId(d.getId());
        role.setVersion(d.getVersion());
        role.setStatus(CommonStatus.valueOf(d.getStatus()));
        role.setCreatedAt(d.getCreatedAt());
        role.clearDomainEvents();
        return role;
    }

    public static RoleDO toDO(Role role) {
        RoleDO d = new RoleDO();
        d.setId(role.getId());
        d.setVersion(role.getVersion());
        d.setName(role.getName());
        d.setCode(role.getCode());
        d.setDescription(role.getDescription());
        d.setStatus(role.getStatus().name());
        d.setCreatedAt(role.getCreatedAt());
        return d;
    }
}
