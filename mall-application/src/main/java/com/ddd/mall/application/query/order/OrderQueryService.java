package com.ddd.mall.application.query.order;

import com.ddd.mall.application.query.order.dto.OrderDetailDto;
import com.ddd.mall.application.query.order.dto.OrderListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderItem;
import com.ddd.mall.domain.order.OrderPageSlice;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.order.OrderStatus;
import com.ddd.mall.domain.order.ShippingAddress;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 订单聚合查询服务。
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * 管理端订单分页列表。
     *
     * @param page    页码
     * @param size    每页条数
     * @param status  订单状态
     * @param keyword 搜索关键字
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<OrderListItemDto> orderList(int page, int size, String status, String keyword) {
        OrderPageSlice slice = orderRepository.findPageForAdmin(page, size, status, keyword);
        List<OrderListItemDto> content = slice.getContent().stream().map(this::toListItemDto).toList();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int totalPages = (int) Math.ceil((double) slice.getTotalElements() / safeSize);
        return new PageResult<>(content, slice.getTotalElements(), totalPages, safePage, safeSize);
    }

    /**
     * 查询订单详情。
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @Transactional(readOnly = true)
    public OrderDetailDto orderDetail(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new DomainException("订单不存在: " + orderNo));
        return toDetailDto(order);
    }

    private OrderListItemDto toListItemDto(Order order) {
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

    private OrderDetailDto toDetailDto(Order order) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setMemberId(order.getMemberId());
        dto.setStatus(toApiStatus(order.getStatus()));
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

    private static String resolveUpdatedAt(Order order) {
        return Stream.of(order.getCancelledAt(), order.getCompletedAt(), order.getShippedAt(),
                        order.getPaidAt(), order.getCreatedAt())
                .flatMap(d -> d == null ? Stream.empty() : Stream.of(d))
                .max(Comparator.naturalOrder())
                .map(LocalDateTime::toString)
                .orElse(null);
    }

    private static String toApiStatus(OrderStatus status) {
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
