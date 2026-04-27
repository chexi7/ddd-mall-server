package com.ddd.mall.application.command.order.handler;

import com.ddd.mall.application.command.order.cmd.CreateOrderCommand;
import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderItem;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.order.ShippingAddress;
import com.ddd.mall.domain.shared.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderHandler {

    private final OrderRepository orderRepository;

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

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return timestamp + random;
    }
}