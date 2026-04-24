package com.ddd.mall.domain.order.event;

import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.shared.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderCreatedEvent implements DomainEvent {
    private final Long orderId;
    private final String orderNo;
    private final Long memberId;
    private final List<OrderItemInfo> items;
    private final LocalDateTime occurredOn;

    public OrderCreatedEvent(Order order) {
        this.orderId = order.getId();
        this.orderNo = order.getOrderNo();
        this.memberId = order.getMemberId();
        this.items = order.getItems().stream()
                .map(i -> new OrderItemInfo(i.getProductId(), i.getSkuId(), i.getQuantity()))
                .collect(Collectors.toList());
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }

    @Getter
    @RequiredArgsConstructor
    public static class OrderItemInfo {
        private final Long productId;
        private final Long skuId;
        private final int quantity;
    }
}
