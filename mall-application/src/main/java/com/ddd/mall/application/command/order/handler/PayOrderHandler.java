package com.ddd.mall.application.command.order.handler;

import com.ddd.mall.application.command.order.cmd.PayOrderCommand;
import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayOrderHandler {

    private final OrderRepository orderRepository;

    @Transactional
    public void handle(PayOrderCommand command) {
        Order order = orderRepository.findByOrderNo(command.getOrderNo())
                .orElseThrow(() -> new DomainException("订单不存在: " + command.getOrderNo()));
        order.pay();
        orderRepository.save(order);
    }
}