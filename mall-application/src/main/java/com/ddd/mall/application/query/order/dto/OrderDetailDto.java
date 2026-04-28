package com.ddd.mall.application.query.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情（管理端 / C端共用）
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单项列表
     */
    private List<OrderItemDto> items;

    /**
     * 收货地址
     */
    private ShippingAddressDto shippingAddress;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {

        /**
         * 订单项ID
         */
        private Long id;

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * SKU编码
         */
        private String skuCode;

        /**
         * 单价
         */
        private BigDecimal unitPrice;

        /**
         * 数量
         */
        private int quantity;

        /**
         * 小计金额
         */
        private BigDecimal subtotal;

        /**
         * 总价
         */
        private BigDecimal totalPrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {

        /**
         * 收件人姓名
         */
        private String receiverName;

        /**
         * 收件人电话
         */
        private String receiverPhone;

        /**
         * 完整地址
         */
        private String fullAddress;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 区县
         */
        private String district;

        /**
         * 详细地址
         */
        private String detailAddress;
    }
}