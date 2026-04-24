package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.order.*;
import com.ddd.mall.domain.shared.Money;
import com.ddd.mall.infrastructure.persistence.dataobject.OrderDO;
import com.ddd.mall.infrastructure.persistence.dataobject.OrderItemDO;

import java.util.stream.Collectors;

public class OrderConverter {

    public static Order toDomain(OrderDO d) {
        Order order = new Order() {};
        order.setId(d.getId());
        order.setVersion(d.getVersion());
        order.setOrderNo(d.getOrderNo());
        order.setMemberId(d.getMemberId());
        order.setTotalAmount(Money.of(d.getTotalAmount()));
        order.setStatus(OrderStatus.valueOf(d.getStatus()));
        order.setCreatedAt(d.getCreatedAt());
        order.setPaidAt(d.getPaidAt());
        order.setShippedAt(d.getShippedAt());
        order.setCompletedAt(d.getCompletedAt());
        order.setCancelledAt(d.getCancelledAt());

        if (d.getReceiverName() != null) {
            order.setShippingAddress(new ShippingAddress(
                    d.getReceiverName(), d.getReceiverPhone(),
                    d.getShippingProvince(), d.getShippingCity(),
                    d.getShippingDistrict(), d.getShippingDetail()));
        }

        for (OrderItemDO itemDO : d.getItems()) {
            order.addItemInternal(toItemDomain(itemDO));
        }
        return order;
    }

    public static OrderDO toDO(Order order) {
        OrderDO d = new OrderDO();
        d.setId(order.getId());
        d.setVersion(order.getVersion());
        d.setOrderNo(order.getOrderNo());
        d.setMemberId(order.getMemberId());
        d.setTotalAmount(order.getTotalAmount().getAmount());
        d.setStatus(order.getStatus().name());
        d.setCreatedAt(order.getCreatedAt());
        d.setPaidAt(order.getPaidAt());
        d.setShippedAt(order.getShippedAt());
        d.setCompletedAt(order.getCompletedAt());
        d.setCancelledAt(order.getCancelledAt());

        ShippingAddress addr = order.getShippingAddress();
        if (addr != null) {
            d.setReceiverName(addr.getReceiverName());
            d.setReceiverPhone(addr.getReceiverPhone());
            d.setShippingProvince(addr.getProvince());
            d.setShippingCity(addr.getCity());
            d.setShippingDistrict(addr.getDistrict());
            d.setShippingDetail(addr.getDetail());
        }

        d.setItems(order.getItems().stream().map(OrderConverter::toItemDO).collect(Collectors.toList()));
        return d;
    }

    private static OrderItem toItemDomain(OrderItemDO d) {
        OrderItem item = new OrderItem() {};
        item.setId(d.getId());
        item.setProductId(d.getProductId());
        item.setSkuId(d.getSkuId());
        item.setProductName(d.getProductName());
        item.setUnitPrice(Money.of(d.getUnitPrice()));
        item.setQuantity(d.getQuantity());
        return item;
    }

    private static OrderItemDO toItemDO(OrderItem item) {
        OrderItemDO d = new OrderItemDO();
        d.setId(item.getId());
        d.setProductId(item.getProductId());
        d.setSkuId(item.getSkuId());
        d.setProductName(item.getProductName());
        d.setUnitPrice(item.getUnitPrice().getAmount());
        d.setQuantity(item.getQuantity());
        return d;
    }
}
