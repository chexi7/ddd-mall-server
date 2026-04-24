package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.RoleDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleDO, Long> {
    Optional<RoleDO> findByCode(String code);
    boolean existsByCode(String code);
    List<RoleDO> findByIdIn(List<Long> ids);
}
