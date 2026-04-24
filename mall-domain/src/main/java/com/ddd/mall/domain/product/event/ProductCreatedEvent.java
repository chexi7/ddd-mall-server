package com.ddd.mall.domain.product.event;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.shared.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductCreatedEvent implements DomainEvent {
    private final Long productId;
    private final String productName;
    private final LocalDateTime occurredOn;

    public ProductCreatedEvent(Product product) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
}
