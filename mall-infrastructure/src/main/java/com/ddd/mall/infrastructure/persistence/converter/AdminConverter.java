package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.admin.Admin;
import com.ddd.mall.domain.shared.CommonStatus;
import com.ddd.mall.infrastructure.persistence.dataobject.AdminDO;

public class AdminConverter {

    public static Admin toDomain(AdminDO d) {
        Admin admin = new Admin(d.getUsername(), d.getPassword(), d.getRealName());
        admin.setId(d.getId());
        admin.setVersion(d.getVersion());
        admin.setPhone(d.getPhone());
        admin.setEmail(d.getEmail());
        admin.setStatus(CommonStatus.valueOf(d.getStatus()));
        admin.setCreatedAt(d.getCreatedAt());
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
