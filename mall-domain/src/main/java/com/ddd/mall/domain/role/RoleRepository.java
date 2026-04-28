package com.ddd.mall.domain.role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findById(Long id);
    Optional<Role> findByCode(String code);
    List<Role> findByIds(List<Long> ids);
    List<Role> findAll();
    boolean existsByCode(String code);
    void save(Role role);
}