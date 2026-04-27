package com.ddd.mall.application.query.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
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
    @Setter
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
    @Setter
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
