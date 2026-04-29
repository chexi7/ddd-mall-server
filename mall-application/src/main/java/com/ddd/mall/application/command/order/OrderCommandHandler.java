package com.ddd.mall.application.command.order;

import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderItem;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.order.ShippingAddress;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单聚合命令处理器，处理订单相关命令。
 * <p>
 * 对应 DDD 铁律：一个命令处理器只操作一个聚合，聚合仅可以在命令处理器中进行操作。
 */
@Service
@RequiredArgsConstructor
public class OrderCommandHandler {

    private final OrderRepository orderRepository;

    /**
     * 处理创建订单命令。
     */
    @Transactional
    public String handle(CreateOrderCommand command) {
        List<OrderItem> items = command.getItems().stream()
                .map(p -> Order.createItem(
                        p.getProductId(), p.getSkuId(), p.getProductName(),
                        Money.of(p.getUnitPrice()), p.getQuantity()))
                .collect(Collectors.toList());

        CreateOrderCommand.ShippingAddressParam addr = command.getShippingAddress();
        ShippingAddress shippingAddress = new ShippingAddress(
                addr.getReceiverName(), addr.getReceiverPhone(),
                addr.getProvince(), addr.getCity(), addr.getDistrict(), addr.getDetail());

        String orderNo = generateOrderNo();
        Order order = new Order(orderNo, command.getMemberId(), items, shippingAddress);
        orderRepository.save(order);
        return orderNo;
    }

    /**
     * 处理支付订单命令。
     */
    @Transactional
    public void handle(PayOrderCommand command) {
        Order order = orderRepository.findByOrderNo(command.getOrderNo())
                .orElseThrow(() -> new DomainException("订单不存在: " + command.getOrderNo()));
        order.pay();
        orderRepository.save(order);
    }

    /**
     * 处理取消订单命令。
     */
    @Transactional
    public void handle(CancelOrderCommand command) {
        Order order = orderRepository.findByOrderNo(command.getOrderNo())
                .orElseThrow(() -> new DomainException("订单不存在: " + command.getOrderNo()));
        order.cancel();
        orderRepository.save(order);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return timestamp + random;
    }
}