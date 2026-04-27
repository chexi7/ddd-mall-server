package com.ddd.mall.application.query.order;

import com.ddd.mall.application.query.order.dto.OrderDetailDto;
import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        dto.setStatus(OrderListQueryHandler.toApiStatus(order.getStatus()));
        dto.setTotalAmount(order.getTotalAmount().getAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(Stream.of(order.getCancelledAt(), order.getCompletedAt(), order.getShippedAt(),
                        order.getPaidAt(), order.getCreatedAt())
                .flatMap(d -> d == null ? Stream.empty() : Stream.of(d))
                .max(Comparator.naturalOrder())
                .orElse(order.getCreatedAt()));
        dto.setPaidAt(order.getPaidAt());

        dto.setItems(order.getItems().stream().map(item -> {
            OrderDetailDto.OrderItemDto itemDto = new OrderDetailDto.OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProductId());
            itemDto.setProductName(item.getProductName());
            itemDto.setSkuCode(item.getSkuId() == null ? "" : "SKU-" + item.getSkuId());
            itemDto.setUnitPrice(item.getUnitPrice().getAmount());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubtotal(item.subtotal().getAmount());
            itemDto.setTotalPrice(item.subtotal().getAmount());
            return itemDto;
        }).collect(Collectors.toList()));

        if (order.getShippingAddress() != null) {
            OrderDetailDto.ShippingAddressDto addrDto = new OrderDetailDto.ShippingAddressDto();
            addrDto.setReceiverName(order.getShippingAddress().getReceiverName());
            addrDto.setReceiverPhone(order.getShippingAddress().getReceiverPhone());
            addrDto.setFullAddress(order.getShippingAddress().fullAddress());
            addrDto.setProvince(order.getShippingAddress().getProvince());
            addrDto.setCity(order.getShippingAddress().getCity());
            addrDto.setDistrict(order.getShippingAddress().getDistrict());
            addrDto.setDetailAddress(order.getShippingAddress().getDetail());
            dto.setShippingAddress(addrDto);
        }
        return dto;
    }
}
