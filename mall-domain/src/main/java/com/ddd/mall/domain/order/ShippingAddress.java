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

    public String fullAddress() { return province + city + district + detail; }
}