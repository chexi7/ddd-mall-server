package com.ddd.mall.domain.menu;

import java.util.List;
import java.util.Optional;

public interface MenuRepository {
    Optional<Menu> findById(Long id);
    List<Menu> findAll();
    List<Menu> findByPermissionCodes(List<String> permissionCodes);
    void save(Menu menu);
    void remove(Menu menu);
}