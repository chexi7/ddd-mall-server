package com.ddd.mall.web.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求参数
 */
@Getter
@Setter
public class CreateOrderRequest {

    /**
     * 下单会员ID
     */
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    /**
     * 订单商品项列表
     */
    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemRequest> items;

    /**
     * 收货地址
     */
    @NotNull(message = "收货地址不能为空")
    @Valid
    private ShippingAddressRequest shippingAddress;

    /**
     * 订单商品项
     */
    @Getter
    @Setter
    public static class OrderItemRequest {
        /**
         * 商品ID
         */
        @NotNull
        private Long productId;

        /**
         * SKU ID
         */
        private Long skuId;

        /**
         * 商品名称
         */
        @NotNull
        private String productName;

        /**
         * 商品单价
         */
        @NotNull
        @Positive
        private BigDecimal unitPrice;

        /**
         * 购买数量
         */
        @Positive
        private int quantity;
    }

    /**
     * 收货地址信息
     */
    @Getter
    @Setter
    public static class ShippingAddressRequest {
        /**
         * 收货人姓名
         */
        @NotNull
        private String receiverName;

        /**
         * 收货人手机号
         */
        @NotNull
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
        @NotNull
        private String detail;
    }
}
