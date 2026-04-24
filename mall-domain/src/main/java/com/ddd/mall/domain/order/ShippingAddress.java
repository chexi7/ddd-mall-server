package com.ddd.mall.domain.order;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 收货地址值对象（不可变）
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ShippingAddress {
    private final String receiverName;
    private final String receiverPhone;
    private final String province;
    private final String city;
    private final String district;
    private final String detail;

    public String fullAddress() { return province + city + district + detail; }
}
