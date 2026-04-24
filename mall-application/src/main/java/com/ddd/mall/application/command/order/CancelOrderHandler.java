package com.ddd.mall.application.command.order;

import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelOrderHandler {

    private final OrderRepository orderRepository;

    @Transactional
    public void handle(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new DomainException("订单不存在: " + orderNo));
        order.cancel();
        orderRepository.save(order);
    }
}
