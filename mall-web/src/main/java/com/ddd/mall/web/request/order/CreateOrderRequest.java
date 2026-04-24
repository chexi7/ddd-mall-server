package com.ddd.mall.web.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "收货地址不能为空")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @Getter
    @Setter
    public static class OrderItemRequest {
        @NotNull private Long productId;
        private Long skuId;
        @NotNull private String productName;
        @NotNull @Positive private BigDecimal unitPrice;
        @Positive private int quantity;
    }

    @Getter
    @Setter
    public static class ShippingAddressRequest {
        @NotNull private String receiverName;
        @NotNull private String receiverPhone;
        private String province;
        private String city;
        private String district;
        @NotNull private String detail;
    }
}
