package com.ddd.mall.domain.order;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口（命令侧）
 */
public interface OrderRepository {

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNo(String orderNo);

    List<Order> findByMemberId(Long memberId);

    void save(Order order);
}