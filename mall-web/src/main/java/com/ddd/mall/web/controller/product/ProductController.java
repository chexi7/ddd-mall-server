package com.ddd.mall.web.controller.product;

import com.ddd.mall.application.command.product.ChangePriceCommand;
import com.ddd.mall.application.command.product.ChangePriceHandler;
import com.ddd.mall.application.command.product.CreateProductCommand;
import com.ddd.mall.application.command.product.CreateProductHandler;
import com.ddd.mall.application.command.product.PublishProductCommand;
import com.ddd.mall.application.command.product.PublishProductHandler;
import com.ddd.mall.web.request.product.ChangePriceRequest;
import com.ddd.mall.web.request.product.CreateProductRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductHandler createProductHandler;
    private final PublishProductHandler publishProductHandler;
    private final ChangePriceHandler changePriceHandler;

    @PostMapping
    public ApiResponse<Long> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                request.getName(), request.getDescription(),
                request.getPrice(), request.getCategory());
        return ApiResponse.ok(createProductHandler.handle(command));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishProduct(@PathVariable Long id) {
        publishProductHandler.handle(new PublishProductCommand(id));
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/price")
    public ApiResponse<Void> changePrice(@PathVariable Long id,
                                          @Valid @RequestBody ChangePriceRequest request) {
        changePriceHandler.handle(new ChangePriceCommand(id, request.getNewPrice()));
        return ApiResponse.ok();
    }
}
