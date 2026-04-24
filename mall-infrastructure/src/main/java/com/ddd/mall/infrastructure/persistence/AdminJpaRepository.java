package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.AdminDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminJpaRepository extends JpaRepository<AdminDO, Long> {
    Optional<AdminDO> findByUsername(String username);
    boolean existsByUsername(String username);
}
