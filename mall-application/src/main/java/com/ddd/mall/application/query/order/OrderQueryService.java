package com.ddd.mall.application.query.order;

import com.ddd.mall.application.query.order.dto.OrderDetailDto;
import com.ddd.mall.application.query.order.dto.OrderListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderItem;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.order.OrderStatus;
import com.ddd.mall.domain.order.ShippingAddress;
import com.ddd.mall.domain.order.query.OrderPageResult;
import com.ddd.mall.domain.order.query.OrderQueryPort;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    /**
     * 订单仓储
     */
    private final OrderRepository orderRepository;

    /**
     * 订单查询端口
     */
    private final OrderQueryPort orderQueryPort;

    /**
     * 管理端订单分页列表。
     *
     * @param query 订单列表查询入参（包含分页 + 筛选条件）
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<OrderListItemDto> orderList(OrderListQuery query) {
        int safePage = Math.max(query.getPageNum(), 1);
        int safeSize = Math.max(query.getPageSize(), 10);
        OrderPageResult slice = orderQueryPort.findPageForAdmin(safePage, safeSize, query.getStatus(), query.getKeyword());
        List<OrderListItemDto> content = slice.getContent().stream().map(this::toListItemDto).toList();
        int totalPages = (int) Math.ceil((double) slice.getTotalElements() / safeSize);
        return PageResult.<OrderListItemDto>builder()
                .data(content)
                .totalCount(slice.getTotalElements())
                .totalPages(totalPages)
                .pageNum(safePage)
                .pageSize(safeSize)
                .build();
    }

    /**
     * 查询订单详情。
     *
     * @param orderNo 订单号（单参数，无需 *Query 对象）
     * @return 订单详情
     */
    @Transactional(readOnly = true)
    public OrderDetailDto orderDetail(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new DomainException("订单不存在: " + orderNo));
        return toDetailDto(order);
    }

    private OrderListItemDto toListItemDto(Order order) {
        List<OrderListItemDto.OrderItemRowDto> itemRows = order.getItems().stream()
                .map(item -> OrderListItemDto.OrderItemRowDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .skuCode(item.getSkuId() == null ? "" : "SKU-" + item.getSkuId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice().getAmount())
                        .totalPrice(item.subtotal() == null ? BigDecimal.ZERO : item.subtotal().getAmount())
                        .build())
                .collect(Collectors.toList());

        OrderListItemDto.ShippingAddressRowDto addrDto = null;
        ShippingAddress addr = order.getShippingAddress();
        if (addr != null) {
            addrDto = OrderListItemDto.ShippingAddressRowDto.builder()
                    .receiverName(addr.getReceiverName())
                    .receiverPhone(addr.getReceiverPhone())
                    .province(addr.getProvince())
                    .city(addr.getCity())
                    .district(addr.getDistrict())
                    .detailAddress(addr.getDetail())
                    .build();
        }

        return OrderListItemDto.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(toApiStatus(order.getStatus()))
                .memberId(order.getMemberId())
                .totalAmount(order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount().getAmount())
                .items(itemRows)
                .shippingAddress(addrDto)
                .createdAt(order.getCreatedAt() == null ? null : order.getCreatedAt().toString())
                .updatedAt(resolveUpdatedAt(order))
                .build();
    }

    private OrderDetailDto toDetailDto(Order order) {
        List<OrderDetailDto.OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderDetailDto.OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .skuCode(item.getSkuId() == null ? "" : "SKU-" + item.getSkuId())
                        .unitPrice(item.getUnitPrice().getAmount())
                        .quantity(item.getQuantity())
                        .subtotal(item.subtotal().getAmount())
                        .totalPrice(item.subtotal().getAmount())
                        .build())
                .collect(Collectors.toList());

        OrderDetailDto.ShippingAddressDto addrDto = null;
        if (order.getShippingAddress() != null) {
            ShippingAddress addr = order.getShippingAddress();
            addrDto = OrderDetailDto.ShippingAddressDto.builder()
                    .receiverName(addr.getReceiverName())
                    .receiverPhone(addr.getReceiverPhone())
                    .fullAddress(addr.fullAddress())
                    .province(addr.getProvince())
                    .city(addr.getCity())
                    .district(addr.getDistrict())
                    .detailAddress(addr.getDetail())
                    .build();
        }

        LocalDateTime updatedAt = Stream.of(order.getCancelledAt(), order.getCompletedAt(), order.getShippedAt(),
                        order.getPaidAt(), order.getCreatedAt())
                .flatMap(d -> d == null ? Stream.empty() : Stream.of(d))
                .max(Comparator.naturalOrder())
                .orElse(order.getCreatedAt());

        return OrderDetailDto.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .memberId(order.getMemberId())
                .status(toApiStatus(order.getStatus()))
                .totalAmount(order.getTotalAmount().getAmount())
                .items(itemDtos)
                .shippingAddress(addrDto)
                .createdAt(order.getCreatedAt())
                .updatedAt(updatedAt)
                .paidAt(order.getPaidAt())
                .build();
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