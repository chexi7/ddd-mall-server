package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.MenuDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuJpaRepository extends JpaRepository<MenuDO, Long> {
    List<MenuDO> findByPermissionCodeIn(List<String> permissionCodes);
    List<MenuDO> findAllByOrderBySortAsc();
}
