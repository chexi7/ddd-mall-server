package com.ddd.mall.application.query.dashboard;

import com.ddd.mall.application.query.dashboard.dto.DashboardStatsDto;
import com.ddd.mall.domain.order.query.OrderQueryPort;
import com.ddd.mall.domain.product.query.ProductQueryPort;
import com.ddd.mall.domain.member.query.MemberQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 仪表盘统计查询服务（应用层）。
 * <p>
 * 通过 CQRS 查询端口接口访问统计数据，
 * 命令侧仓储接口仅保留写操作，读操作分离到 QueryPort。
 */
@Service
@RequiredArgsConstructor
public class DashboardQueryService {

    /**
     * 商品查询端口
     */
    private final ProductQueryPort productQueryPort;

    /**
     * 订单查询端口
     */
    private final OrderQueryPort orderQueryPort;

    /**
     * 会员查询端口
     */
    private final MemberQueryPort memberQueryPort;

    /**
     * 加载仪表盘统计数据
     */
    @Transactional(readOnly = true)
    public DashboardStatsDto dashboardStats() {
        long totalProducts = productQueryPort.countTotal();
        long totalOrders = orderQueryPort.countTotal();
        long totalMembers = memberQueryPort.countTotal();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayOrders = orderQueryPort.countByCreatedAtSince(startOfDay);
        BigDecimal revenue = orderQueryPort.sumTotalAmountSince(startOfDay);
        double todayRevenue = revenue == null ? 0d : revenue.doubleValue();

        return DashboardStatsDto.builder()
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalMembers(totalMembers)
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .build();
    }
}