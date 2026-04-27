package com.ddd.mall.domain.admin;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {
    Optional<Admin> findById(Long id);
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);

    /**
     * 全部管理员（含角色 ID），用于管理端列表
     */
    List<Admin> findAllAdmins();

    void save(Admin admin);
}
