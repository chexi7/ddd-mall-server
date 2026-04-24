package com.ddd.mall.domain.inventory.event;

import com.ddd.mall.domain.shared.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InventoryDeductedEvent implements DomainEvent {
    private final Long productId;
    private final int quantity;
    private final LocalDateTime occurredOn;

    public InventoryDeductedEvent(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
}
