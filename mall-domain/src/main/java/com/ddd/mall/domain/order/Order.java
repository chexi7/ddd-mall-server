package com.ddd.mall.domain.order;

import com.ddd.mall.domain.order.event.OrderCancelledEvent;
import com.ddd.mall.domain.order.event.OrderCreatedEvent;
import com.ddd.mall.domain.order.event.OrderPaidEvent;
import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 订单聚合根
 * <p>
 * 状态机：PENDING_PAYMENT → PAID → SHIPPED → COMPLETED | CANCELLED
 */
@Getter
public class Order extends AggregateRoot {

    @Setter
    private String orderNo;
    @Setter
    private Long memberId;
    private final List<OrderItem> items = new ArrayList<>();
    @Setter
    private Money totalAmount;
    @Setter
    private OrderStatus status;
    @Setter
    private ShippingAddress shippingAddress;
    @Setter
    private LocalDateTime createdAt;
    @Setter
    private LocalDateTime paidAt;
    @Setter
    private LocalDateTime shippedAt;
    @Setter
    private LocalDateTime completedAt;
    @Setter
    private LocalDateTime cancelledAt;

    protected Order() {
    }

    public Order(String orderNo, Long memberId, List<OrderItem> items, ShippingAddress shippingAddress) {
        if (items == null || items.isEmpty()) throw new DomainException("订单至少包含一个商品");
        if (shippingAddress == null) throw new DomainException("收货地址不能为空");
        this.orderNo = orderNo;
        this.memberId = memberId;
        this.items.addAll(items);
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
        this.totalAmount = calculateTotalAmount();
        registerEvent(new OrderCreatedEvent(this));
    }

    public void pay() {
        if (this.status != OrderStatus.PENDING_PAYMENT)
            throw new DomainException("只有待支付的订单才能支付，当前状态: " + this.status);
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
        registerEvent(new OrderPaidEvent(this));
    }

    public void ship() {
        if (this.status != OrderStatus.PAID)
            throw new DomainException("只有已支付的订单才能发货，当前状态: " + this.status);
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != OrderStatus.SHIPPED)
            throw new DomainException("只有已发货的订单才能确认收货，当前状态: " + this.status);
        this.status = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING_PAYMENT)
            throw new DomainException("只有待支付的订单才能取消，当前状态: " + this.status);
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        registerEvent(new OrderCancelledEvent(this));
    }

    private Money calculateTotalAmount() {
        return items.stream().map(OrderItem::subtotal).reduce(Money.zero(), Money::add);
    }

    public static OrderItem createItem(Long productId, Long skuId, String productName, Money unitPrice, int quantity) {
        return new OrderItem(productId, skuId, productName, unitPrice, quantity);
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * 仓储重建用
     */
    public void addItemInternal(OrderItem item) {
        this.items.add(item);
    }
}
