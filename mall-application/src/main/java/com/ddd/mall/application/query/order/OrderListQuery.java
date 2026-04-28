package com.ddd.mall.application.query.order;

import com.ddd.mall.application.query.support.PageQuery;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订单列表查询入参。
 * <p>
 * 分页字段继承 PageQuery（可变），业务筛选字段 final（不可变），
 * 与 Command 侧的不可变原则保持对称。
 */
@Getter
@RequiredArgsConstructor
public class OrderListQuery extends PageQuery {

    /**
     * 订单状态
     */
    private final String status;

    /**
     * 搜索关键字
     */
    private final String keyword;
}