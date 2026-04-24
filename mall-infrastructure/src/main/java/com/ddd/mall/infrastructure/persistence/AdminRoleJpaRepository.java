package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.AdminRoleDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminRoleJpaRepository extends JpaRepository<AdminRoleDO, Long> {
    List<AdminRoleDO> findByAdminId(Long adminId);
    void deleteByAdminId(Long adminId);
}
