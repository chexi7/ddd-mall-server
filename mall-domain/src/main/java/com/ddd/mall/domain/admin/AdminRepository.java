package com.ddd.mall.domain.admin;

import java.util.Optional;

public interface AdminRepository {
    Optional<Admin> findById(Long id);
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    void save(Admin admin);
}
