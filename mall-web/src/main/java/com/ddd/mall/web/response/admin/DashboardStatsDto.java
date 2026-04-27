package com.ddd.mall.web.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 管理端仪表盘统计
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalProducts;
    private long totalOrders;
    private long totalMembers;
    private long todayOrders;
    private double todayRevenue;
}
