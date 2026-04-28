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
    private Long id;
    private String orderNo;
    private Long memberId;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private ShippingAddressDto shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String skuCode;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subtotal;
        private BigDecimal totalPrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        private String receiverName;
        private String receiverPhone;
        private String fullAddress;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
    }
}