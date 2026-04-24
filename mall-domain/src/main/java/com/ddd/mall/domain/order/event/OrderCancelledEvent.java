package com.ddd.mall.domain.order.event;

import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.shared.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderCancelledEvent implements DomainEvent {
    private final Long orderId;
    private final String orderNo;
    private final Long memberId;
    private final List<OrderCreatedEvent.OrderItemInfo> items;
    private final LocalDateTime occurredOn;

    public OrderCancelledEvent(Order order) {
        this.orderId = order.getId();
        this.orderNo = order.getOrderNo();
        this.memberId = order.getMemberId();
        this.items = order.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderItemInfo(i.getProductId(), i.getSkuId(), i.getQuantity()))
                .collect(Collectors.toList());
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
}
