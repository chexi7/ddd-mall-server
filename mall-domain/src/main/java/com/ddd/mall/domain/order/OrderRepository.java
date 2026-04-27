package com.ddd.mall.domain.order;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口
 */
public interface OrderRepository {

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(String orderNo);

    List<Order> findByMemberId(Long memberId);

    /**
     * 管理端分页查询，page 从 1 开始；status 为前端展示状态（如 CREATED）或 null 表示全部
     */
    OrderPageSlice findPageForAdmin(int page, int size, String statusApi, String orderNoKeyword);

    void save(Order order);
}
