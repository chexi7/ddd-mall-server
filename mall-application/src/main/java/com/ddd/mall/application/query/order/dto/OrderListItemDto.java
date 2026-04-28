package com.ddd.mall.application.query.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端订单列表项（字段形态对齐管理前端）
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemDto {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单项列表
     */
    private List<OrderItemRowDto> items;

    /**
     * 收货地址
     */
    private ShippingAddressRowDto shippingAddress;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRowDto {

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
         * 数量
         */
        private Integer quantity;

        /**
         * 单价
         */
        private BigDecimal unitPrice;

        /**
         * 总价
         */
        private BigDecimal totalPrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressRowDto {

        /**
         * 收件人姓名
         */
        private String receiverName;

        /**
         * 收件人电话
         */
        private String receiverPhone;

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