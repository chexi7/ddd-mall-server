package com.ddd.mall.web.controller.order;

import com.ddd.mall.application.command.order.CancelOrderHandler;
import com.ddd.mall.application.command.order.CreateOrderCommand;
import com.ddd.mall.application.command.order.CreateOrderHandler;
import com.ddd.mall.application.command.order.PayOrderHandler;
import com.ddd.mall.application.query.order.OrderDetailQueryHandler;
import com.ddd.mall.application.query.order.dto.OrderDetailDto;
import com.ddd.mall.web.request.order.CreateOrderRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderHandler createOrderHandler;
    private final PayOrderHandler payOrderHandler;
    private final CancelOrderHandler cancelOrderHandler;
    private final OrderDetailQueryHandler orderDetailQueryHandler;

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

        return ApiResponse.ok(createOrderHandler.handle(
                new CreateOrderCommand(request.getMemberId(), items, shippingAddress)));
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<Void> payOrder(@PathVariable String orderNo) {
        payOrderHandler.handle(orderNo);
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderNo) {
        cancelOrderHandler.handle(orderNo);
        return ApiResponse.ok();
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailDto> getOrder(@PathVariable String orderNo) {
        return ApiResponse.ok(orderDetailQueryHandler.handle(orderNo));
    }
}
