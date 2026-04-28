package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.menu.Menu;
import com.ddd.mall.domain.menu.MenuRepository;
import com.ddd.mall.infrastructure.persistence.MenuJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.MenuConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuJpaRepository menuJpaRepository;

    @Override
    public Optional<Menu> findById(Long id) {
        return menuJpaRepository.findById(id).map(MenuConverter::toDomain);
    }

    @Override
    public List<Menu> findAll() {
        return menuJpaRepository.findAllByOrderBySortAsc().stream()
                .map(MenuConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Menu> findByPermissionCodes(List<String> permissionCodes) {
        return menuJpaRepository.findByPermissionCodeIn(permissionCodes).stream()
                .map(MenuConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public void save(Menu menu) {
        menu.setId(menuJpaRepository.save(MenuConverter.toDO(menu)).getId());
    }

    @Override
    public void remove(Menu menu) {
        menuJpaRepository.deleteById(menu.getId());
    }
}
