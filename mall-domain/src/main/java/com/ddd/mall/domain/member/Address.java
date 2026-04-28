package com.ddd.mall.domain.member;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 地址值对象（不可变）
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Address {

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

    /**
     * 邮编
     */
    private final String zipCode;

    public String fullAddress() { return province + city + district + detail; }
}