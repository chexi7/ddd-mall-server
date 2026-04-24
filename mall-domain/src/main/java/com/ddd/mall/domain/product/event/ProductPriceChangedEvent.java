package com.ddd.mall.domain.product.event;

import com.ddd.mall.domain.shared.DomainEvent;
import com.ddd.mall.domain.shared.Money;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class ProductPriceChangedEvent implements DomainEvent {
    private final Long productId;
    private final Money oldPrice;
    private final Money newPrice;
    private final LocalDateTime occurredOn;

    public ProductPriceChangedEvent(Long productId, Money oldPrice, Money newPrice) {
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
}
