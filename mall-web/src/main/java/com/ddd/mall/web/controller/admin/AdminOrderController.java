package com.ddd.mall.web.controller.admin;

import com.ddd.mall.application.query.order.OrderQueryService;
import com.ddd.mall.application.query.order.dto.OrderListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.infrastructure.auth.RequireLogin;
import com.ddd.mall.infrastructure.auth.RequirePermission;
import com.ddd.mall.infrastructure.auth.UserType;
import com.ddd.mall.web.response.ApiResponse;
import com.ddd.mall.web.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端订单查询（与会员端 /api/orders 路径分离）
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@RequireLogin(UserType.ADMIN)
public class AdminOrderController {

    private final OrderQueryService orderQueryService;

    @GetMapping
    @RequirePermission("order:view")
    public ApiResponse<PageResponse<OrderListItemDto>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        PageResult<OrderListItemDto> r = orderQueryService.orderList(page, size, status, keyword);
        return ApiResponse.ok(new PageResponse<>(
                r.getContent(), r.getTotalElements(), r.getTotalPages(), r.getPage(), r.getSize()));
    }
}
