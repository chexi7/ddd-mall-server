package com.ddd.mall.domain.inventory.event;

import com.ddd.mall.domain.shared.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InventoryDeductedEvent implements DomainEvent {

    /**
     * 商品ID
     */
    private final Long productId;

    /**
     * 扣减数量
     */
    private final int quantity;

    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;

    public InventoryDeductedEvent(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
}