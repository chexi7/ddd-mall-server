package com.ddd.mall.web.controller.order;

import com.ddd.mall.application.command.order.CancelOrderCommand;
import com.ddd.mall.application.command.order.CreateOrderCommand;
import com.ddd.mall.application.command.order.OrderCommandHandler;
import com.ddd.mall.application.command.order.PayOrderCommand;
import com.ddd.mall.application.query.order.OrderQueryService;
import com.ddd.mall.application.query.order.dto.OrderDetailDto;
import com.ddd.mall.web.request.order.CreateOrderRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单接口
 * 提供订单创建 支付 取消 和详情查询能力
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCommandHandler orderCommandHandler;
    private final OrderQueryService orderQueryService;

    /**
     * 创建订单
     *
     * @param request 创建订单请求参数
     * @return 订单号
     */
    @PostMapping
    public ApiResponse<String> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        List<CreateOrderCommand.OrderItemParam> items = request.getItems().stream()
                .map(i -> new CreateOrderCommand.OrderItemParam(
                        i.getProductId(), i.getSkuId(), i.getProductName(),
                        i.getUnitPrice(), i.getQuantity()))
                .collect(Collectors.toList());

        CreateOrderRequest.ShippingAddressRequest addr = request.getShippingAddress();
        CreateOrderCommand.ShippingAddressParam shippingAddress = new CreateOrderCommand.ShippingAddressParam(
                addr.getReceiverName(), addr.getReceiverPhone(),
                addr.getProvince(), addr.getCity(), addr.getDistrict(), addr.getDetail());

        return ApiResponse.ok(orderCommandHandler.handle(
                new CreateOrderCommand(request.getMemberId(), items, shippingAddress)));
    }

    /**
     * 支付订单
     *
     * @param orderNo 订单号
     * @return 空响应
     */
    @PostMapping("/{orderNo}/pay")
    public ApiResponse<Void> payOrder(@PathVariable String orderNo) {
        orderCommandHandler.handle(new PayOrderCommand(orderNo));
        return ApiResponse.ok();
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @return 空响应
     */
    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderNo) {
        orderCommandHandler.handle(new CancelOrderCommand(orderNo));
        return ApiResponse.ok();
    }

    /**
     * 查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailDto> getOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(orderQueryService.orderDetail(orderNo));
    }
}
