package com.ddd.mall.domain.order.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单查询端口（CQRS 读侧）
 * 应用层 QueryService 通过此接口查询数据，基础设施层实现。
 */
public interface OrderQueryPort {

    /**
     * 管理端分页查询，page 从 1 开始；status 为前端展示状态或 null 表示全部
     */
    OrderPageResult findPageForAdmin(int page, int size, String statusApi, String orderNoKeyword);

    /**
     * 订单总数
     */
    long countTotal();

    /**
     * 今日新增订单数
     */
    long countByCreatedAtSince(LocalDateTime since);

    /**
     * 今日营收
     */
    BigDecimal sumTotalAmountSince(LocalDateTime since);
}