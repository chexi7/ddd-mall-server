package com.ddd.mall.application.query.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端订单列表项（字段形态对齐管理前端）
 */
@Getter
@Setter
public class OrderListItemDto {
    private Long id;
    private String orderNo;
    private String status;
    private Long memberId;
    private Double totalAmount;
    private List<OrderItemRowDto> items = new ArrayList<>();
    private ShippingAddressRowDto shippingAddress;
    private String createdAt;
    private String updatedAt;

    @Getter
    @Setter
    public static class OrderItemRowDto {
        private Long id;
        private Long productId;
        private String productName;
        private String skuCode;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
    }

    @Getter
    @Setter
    public static class ShippingAddressRowDto {
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
    }
}
