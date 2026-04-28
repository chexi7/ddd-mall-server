package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.inventory.Inventory;
import com.ddd.mall.domain.inventory.InventoryRepository;
import com.ddd.mall.infrastructure.persistence.InventoryJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.InventoryConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.InventoryDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).map(InventoryConverter::toDomain);
    }

    @Override
    public void save(Inventory inventory) {
        InventoryDO saved = jpaRepository.save(InventoryConverter.toDO(inventory));
        DomainObjectReconstructor.setIdAndVersion(inventory, saved.getId(), saved.getVersion());
        inventory.getDomainEvents().forEach(eventPublisher::publishEvent);
        inventory.clearDomainEvents();
    }
}
