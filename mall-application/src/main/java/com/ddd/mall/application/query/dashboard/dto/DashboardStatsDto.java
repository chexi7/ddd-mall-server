package com.ddd.mall.application.query.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 管理端仪表盘统计读模型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {

    /**
     * 商品总数
     */
    private long totalProducts;

    /**
     * 订单总数
     */
    private long totalOrders;

    /**
     * 会员总数
     */
    private long totalMembers;

    /**
     * 今日订单数
     */
    private long todayOrders;

    /**
     * 今日营收
     */
    private double todayRevenue;
}