package com.ddd.mall.application.command.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CreateOrderCommand {
    private final Long memberId;
    private final List<OrderItemParam> items;
    private final ShippingAddressParam shippingAddress;

    @Getter
    @RequiredArgsConstructor
    public static class OrderItemParam {
        private final Long productId;
        private final Long skuId;
        private final String productName;
        private final BigDecimal unitPrice;
        private final int quantity;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ShippingAddressParam {
        private final String receiverName;
        private final String receiverPhone;
        private final String province;
        private final String city;
        private final String district;
        private final String detail;
    }
}