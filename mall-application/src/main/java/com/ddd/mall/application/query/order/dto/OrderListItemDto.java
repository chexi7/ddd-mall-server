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
    private Long id;
    private String orderNo;
    private String status;
    private Long memberId;
    private BigDecimal totalAmount;
    private List<OrderItemRowDto> items;
    private ShippingAddressRowDto shippingAddress;
    private String createdAt;
    private String updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRowDto {
        private Long id;
        private Long productId;
        private String productName;
        private String skuCode;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressRowDto {
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
    }
}