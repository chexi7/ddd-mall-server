package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductRepository;
import com.ddd.mall.domain.shared.DomainEvent;
import com.ddd.mall.infrastructure.persistence.ProductJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.ProductConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(ProductConverter::toDomain);
    }

    @Override
    public void save(Product product) {
        ProductDO saved = jpaRepository.save(ProductConverter.toDO(product));
        DomainObjectReconstructor.setIdAndVersion(product, saved.getId(), saved.getVersion());
        List<DomainEvent> events = product.getDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        product.clearDomainEvents();
    }

    @Override
    public void remove(Product product) {
        jpaRepository.deleteById(product.getId());
    }
}