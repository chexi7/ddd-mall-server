package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.shared.CommonStatus;
import com.ddd.mall.infrastructure.persistence.dataobject.AdminDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdminConverter {

    public static Admin toDomain(AdminDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("username", d.getUsername());
        fields.put("password", d.getPassword());
        fields.put("realName", d.getRealName());
        fields.put("phone", d.getPhone());
        fields.put("email", d.getEmail());
        fields.put("status", CommonStatus.valueOf(d.getStatus()));
        fields.put("createdAt", d.getCreatedAt());

        Admin admin = DomainObjectReconstructor.reconstruct(Admin.class, fields);
        admin.clearDomainEvents();
        return admin;
    }

    public static AdminDO toDO(Admin admin) {
        AdminDO d = new AdminDO();
        d.setId(admin.getId());
        d.setVersion(admin.getVersion());
        d.setUsername(admin.getUsername());
        d.setPassword(admin.getPassword());
        d.setRealName(admin.getRealName());
        d.setPhone(admin.getPhone());
        d.setEmail(admin.getEmail());
        d.setStatus(admin.getStatus().name());
        d.setCreatedAt(admin.getCreatedAt());
        return d;
    }
}