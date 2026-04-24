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
    private final String province;
    private final String city;
    private final String district;
    private final String detail;
    private final String zipCode;

    public String fullAddress() { return province + city + district + detail; }
}
