package com.ddd.mall.domain.order.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 管理端订单分页查询结果（读模型）
 */
@Getter
@RequiredArgsConstructor
public class OrderPageResult {
    private final List<com.ddd.mall.domain.order.Order> content;
    private final long totalElements;
}