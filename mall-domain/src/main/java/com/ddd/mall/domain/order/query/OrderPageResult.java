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

    /**
     * 订单列表
     */
    private final List<com.ddd.mall.domain.order.Order> content;

    /**
     * 总记录数
     */
    private final long totalElements;
}