package com.ddd.mall.domain.admin;

import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 角色聚合根
 */
@Getter
public class Role extends AggregateRoot {

    @Setter private String name;
    @Setter private String code;
    @Setter private String description;
    @Setter private AdminStatus status;
    private final List<String> permissionCodes = new ArrayList<>();
    @Setter private LocalDateTime createdAt;

    protected Role() {}

    public Role(String name, String code, String description) {
        if (name == null || name.isBlank()) throw new DomainException("角色名称不能为空");
        if (code == null || code.isBlank()) throw new DomainException("角色编码不能为空");
        this.name = name;
        this.code = code;
        this.description = description;
        this.status = AdminStatus.ENABLED;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 分配权限（全量替换）
     */
    public void assignPermissions(List<String> newPermissionCodes) {
        this.permissionCodes.clear();
        if (newPermissionCodes != null) {
            this.permissionCodes.addAll(newPermissionCodes);
        }
    }

    public boolean hasPermission(String permissionCode) {
        return this.permissionCodes.contains(permissionCode);
    }

    public void enable() { this.status = AdminStatus.ENABLED; }
    public void disable() { this.status = AdminStatus.DISABLED; }

    public List<String> getPermissionCodes() {
        return Collections.unmodifiableList(permissionCodes);
    }

    /** 仓储重建用 */
    public void addPermissionCodeInternal(String code) {
        this.permissionCodes.add(code);
    }
}
