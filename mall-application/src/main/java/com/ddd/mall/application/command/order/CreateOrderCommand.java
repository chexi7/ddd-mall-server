package com.ddd.mall.application.command.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CreateOrderCommand {

    /**
     * 会员ID
     */
    private final Long memberId;

    /**
     * 订单项参数列表
     */
    private final List<OrderItemParam> items;

    /**
     * 收货地址参数
     */
    private final ShippingAddressParam shippingAddress;

    @Getter
    @RequiredArgsConstructor
    public static class OrderItemParam {

        /**
         * 商品ID
         */
        private final Long productId;

        /**
         * SKU ID
         */
        private final Long skuId;

        /**
         * 商品名称
         */
        private final String productName;

        /**
         * 单价
         */
        private final BigDecimal unitPrice;

        /**
         * 数量
         */
        private final int quantity;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ShippingAddressParam {

        /**
         * 收件人姓名
         */
        private final String receiverName;

        /**
         * 收件人电话
         */
        private final String receiverPhone;

        /**
         * 省份
         */
        private final String province;

        /**
         * 城市
         */
        private final String city;

        /**
         * 区县
         */
        private final String district;

        /**
         * 详细地址
         */
        private final String detail;
    }
}