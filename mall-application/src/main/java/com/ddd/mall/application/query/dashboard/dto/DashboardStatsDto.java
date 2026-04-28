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
    private long totalProducts;
    private long totalOrders;
    private long totalMembers;
    private long todayOrders;
    private double todayRevenue;
}