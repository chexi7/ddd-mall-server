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
 * 后台管理员聚合根
 */
@Getter
public class Admin extends AggregateRoot {

    @Setter private String username;
    @Setter private String password;
    @Setter private String realName;
    @Setter private String phone;
    @Setter private String email;
    @Setter private AdminStatus status;
    private final List<Long> roleIds = new ArrayList<>();
    @Setter private LocalDateTime createdAt;

    protected Admin() {}

    public Admin(String username, String password, String realName) {
        if (username == null || username.isBlank()) throw new DomainException("用户名不能为空");
        if (password == null || password.length() < 6) throw new DomainException("密码长度不能少于6位");
        if (realName == null || realName.isBlank()) throw new DomainException("真实姓名不能为空");
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.status = AdminStatus.ENABLED;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 验证密码（明文比较，实际项目应用 BCrypt）
     */
    public boolean verifyPassword(String rawPassword) {
        return this.password != null && this.password.equals(rawPassword);
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) throw new DomainException("原密码错误");
        if (newPassword == null || newPassword.length() < 6) throw new DomainException("新密码长度不能少于6位");
        this.password = newPassword;
    }

    public void enable() {
        this.status = AdminStatus.ENABLED;
    }

    public void disable() {
        this.status = AdminStatus.DISABLED;
    }

    public boolean isEnabled() {
        return this.status == AdminStatus.ENABLED;
    }

    /**
     * 分配角色（全量替换）
     */
    public void assignRoles(List<Long> newRoleIds) {
        this.roleIds.clear();
        if (newRoleIds != null) {
            this.roleIds.addAll(newRoleIds);
        }
    }

    public List<Long> getRoleIds() {
        return Collections.unmodifiableList(roleIds);
    }

    /** 仓储重建用 */
    public void addRoleIdInternal(Long roleId) {
        this.roleIds.add(roleId);
    }
}
