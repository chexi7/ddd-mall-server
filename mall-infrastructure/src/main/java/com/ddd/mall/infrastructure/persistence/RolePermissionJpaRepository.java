package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.RolePermissionDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionDO, Long> {
    List<RolePermissionDO> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}
