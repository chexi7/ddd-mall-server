package com.ddd.mall.domain.product.event;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.shared.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductCreatedEvent implements DomainEvent {

    /**
     * 商品ID
     */
    private final Long productId;

    /**
     * 商品名称
     */
    private final String productName;

    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;

    public ProductCreatedEvent(Product product) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
}