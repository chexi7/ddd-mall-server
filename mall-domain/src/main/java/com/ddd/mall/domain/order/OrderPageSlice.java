package com.ddd.mall.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 管理端订单分页查询结果
 */
@Getter
@RequiredArgsConstructor
public class OrderPageSlice {
    private final List<Order> content;
    private final long totalElements;
}
