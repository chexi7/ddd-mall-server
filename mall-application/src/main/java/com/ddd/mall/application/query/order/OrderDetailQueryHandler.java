package com.ddd.mall.application.query.order;

import com.ddd.mall.application.query.order.dto.OrderDetailDto;
import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailQueryHandler {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderDetailDto handle(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new DomainException("订单不存在: " + orderNo));
        return toDto(order);
    }

    private OrderDetailDto toDto(Order order) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setMemberId(order.getMemberId());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount().getAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaidAt(order.getPaidAt());

        dto.setItems(order.getItems().stream().map(item -> {
            OrderDetailDto.OrderItemDto itemDto = new OrderDetailDto.OrderItemDto();
            itemDto.setProductId(item.getProductId());
            itemDto.setProductName(item.getProductName());
            itemDto.setUnitPrice(item.getUnitPrice().getAmount());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubtotal(item.subtotal().getAmount());
            return itemDto;
        }).collect(Collectors.toList()));

        if (order.getShippingAddress() != null) {
            OrderDetailDto.ShippingAddressDto addrDto = new OrderDetailDto.ShippingAddressDto();
            addrDto.setReceiverName(order.getShippingAddress().getReceiverName());
            addrDto.setReceiverPhone(order.getShippingAddress().getReceiverPhone());
            addrDto.setFullAddress(order.getShippingAddress().fullAddress());
            dto.setShippingAddress(addrDto);
        }
        return dto;
    }
}
