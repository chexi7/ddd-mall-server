package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.shared.CommonStatus;
import com.ddd.mall.domain.role.Role;
import com.ddd.mall.infrastructure.persistence.dataobject.RoleDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;

public class RoleConverter {

    public static Role toDomain(RoleDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("name", d.getName());
        fields.put("code", d.getCode());
        fields.put("description", d.getDescription());
        fields.put("status", CommonStatus.valueOf(d.getStatus()));
        fields.put("createdAt", d.getCreatedAt());

        Role role = DomainObjectReconstructor.reconstruct(Role.class, fields);
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