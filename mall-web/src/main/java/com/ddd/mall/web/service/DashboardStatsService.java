package com.ddd.mall.web.service;

import com.ddd.mall.web.response.admin.DashboardStatsDto;
import com.ddd.mall.infrastructure.persistence.MemberJpaRepository;
import com.ddd.mall.infrastructure.persistence.OrderJpaRepository;
import com.ddd.mall.infrastructure.persistence.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 仪表盘统计（读模型，直接访问 JPA 仓储）
 */
@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final ProductJpaRepository productJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    public DashboardStatsDto load() {
        long totalProducts = productJpaRepository.count();
        long totalOrders = orderJpaRepository.count();
        long totalMembers = memberJpaRepository.count();
        var startOfDay = LocalDate.now().atStartOfDay();
        long todayOrders = orderJpaRepository.countByCreatedAtGreaterThanEqual(startOfDay);
        BigDecimal revenue = orderJpaRepository.sumTotalAmountSince(startOfDay);
        double todayRevenue = revenue == null ? 0d : revenue.doubleValue();
        return new DashboardStatsDto(totalProducts, totalOrders, totalMembers, todayOrders, todayRevenue);
    }
}
