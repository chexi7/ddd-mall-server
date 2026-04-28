package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.order.*;
import com.ddd.mall.domain.shared.Money;
import com.ddd.mall.infrastructure.persistence.dataobject.OrderDO;
import com.ddd.mall.infrastructure.persistence.dataobject.OrderItemDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderConverter {

    public static Order toDomain(OrderDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("orderNo", d.getOrderNo());
        fields.put("memberId", d.getMemberId());
        fields.put("totalAmount", Money.of(d.getTotalAmount()));
        fields.put("status", OrderStatus.valueOf(d.getStatus()));
        fields.put("createdAt", d.getCreatedAt());
        fields.put("paidAt", d.getPaidAt());
        fields.put("shippedAt", d.getShippedAt());
        fields.put("completedAt", d.getCompletedAt());
        fields.put("cancelledAt", d.getCancelledAt());
        if (d.getReceiverName() != null) {
            fields.put("shippingAddress", new ShippingAddress(
                    d.getReceiverName(), d.getReceiverPhone(),
                    d.getShippingProvince(), d.getShippingCity(),
                    d.getShippingDistrict(), d.getShippingDetail()));
        }

        Order order = DomainObjectReconstructor.reconstruct(Order.class, fields);
        for (OrderItemDO itemDO : d.getItems()) {
            order.addItemInternal(toItemDomain(itemDO));
        }
        order.clearDomainEvents();
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
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("productId", d.getProductId());
        fields.put("skuId", d.getSkuId());
        fields.put("productName", d.getProductName());
        fields.put("unitPrice", Money.of(d.getUnitPrice()));
        fields.put("quantity", d.getQuantity());
        return DomainObjectReconstructor.reconstruct(OrderItem.class, fields);
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