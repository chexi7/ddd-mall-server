package com.ddd.mall.domain.role;

import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.CommonStatus;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.ReconstructionOnly;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 角色聚合根
 */
@Getter
@ReconstructionOnly
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends AggregateRoot {

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色状态
     */
    private CommonStatus status;

    /**
     * 已分配的权限编码列表
     */
    private final List<String> permissionCodes = new ArrayList<>();

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    public Role(String name, String code, String description) {
        if (name == null || name.isBlank()) throw new DomainException("角色名称不能为空");
        if (code == null || code.isBlank()) throw new DomainException("角色编码不能为空");
        this.name = name;
        this.code = code;
        this.description = description;
        this.status = CommonStatus.ENABLED;
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

    public void enable() { this.status = CommonStatus.ENABLED; }
    public void disable() { this.status = CommonStatus.DISABLED; }

    public List<String> getPermissionCodes() {
        return Collections.unmodifiableList(permissionCodes);
    }

    /** 仓储重建用 */
    public void addPermissionCodeInternal(String code) {
        this.permissionCodes.add(code);
    }
}