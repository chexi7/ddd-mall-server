package com.ddd.mall.application.query.order;

import com.ddd.mall.application.query.order.dto.OrderListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderItem;
import com.ddd.mall.domain.order.OrderPageSlice;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.order.OrderStatus;
import com.ddd.mall.domain.order.ShippingAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 管理端订单分页列表
 */
@Service
@RequiredArgsConstructor
public class OrderListQueryHandler {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PageResult<OrderListItemDto> handle(int page, int size, String status, String keyword) {
        OrderPageSlice slice = orderRepository.findPageForAdmin(page, size, status, keyword);
        List<OrderListItemDto> content = slice.getContent().stream().map(this::toDto).toList();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int totalPages = (int) Math.ceil((double) slice.getTotalElements() / safeSize);
        return new PageResult<>(content, slice.getTotalElements(), totalPages, safePage, safeSize);
    }

    private OrderListItemDto toDto(Order order) {
        OrderListItemDto dto = new OrderListItemDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setStatus(toApiStatus(order.getStatus()));
        dto.setMemberId(order.getMemberId());
        dto.setTotalAmount(order.getTotalAmount() == null ? 0d : order.getTotalAmount().getAmount().doubleValue());
        dto.setCreatedAt(order.getCreatedAt() == null ? null : order.getCreatedAt().toString());
        dto.setUpdatedAt(resolveUpdatedAt(order));

        for (OrderItem item : order.getItems()) {
            OrderListItemDto.OrderItemRowDto row = new OrderListItemDto.OrderItemRowDto();
            row.setId(item.getId());
            row.setProductId(item.getProductId());
            row.setProductName(item.getProductName());
            row.setSkuCode(item.getSkuId() == null ? "" : "SKU-" + item.getSkuId());
            row.setQuantity(item.getQuantity());
            row.setUnitPrice(item.getUnitPrice() == null ? 0d : item.getUnitPrice().getAmount().doubleValue());
            row.setTotalPrice(item.subtotal() == null ? 0d : item.subtotal().getAmount().doubleValue());
            dto.getItems().add(row);
        }

        ShippingAddress addr = order.getShippingAddress();
        if (addr != null) {
            OrderListItemDto.ShippingAddressRowDto a = new OrderListItemDto.ShippingAddressRowDto();
            a.setReceiverName(addr.getReceiverName());
            a.setReceiverPhone(addr.getReceiverPhone());
            a.setProvince(addr.getProvince());
            a.setCity(addr.getCity());
            a.setDistrict(addr.getDistrict());
            a.setDetailAddress(addr.getDetail());
            dto.setShippingAddress(a);
        }
        return dto;
    }

    private static String resolveUpdatedAt(Order order) {
        return Stream.of(order.getCancelledAt(), order.getCompletedAt(), order.getShippedAt(),
                        order.getPaidAt(), order.getCreatedAt())
                .flatMap(d -> d == null ? Stream.empty() : Stream.of(d))
                .max(Comparator.naturalOrder())
                .map(LocalDateTime::toString)
                .orElse(null);
    }

    static String toApiStatus(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING_PAYMENT -> "CREATED";
            case COMPLETED -> "DELIVERED";
            default -> status.name();
        };
    }
}
