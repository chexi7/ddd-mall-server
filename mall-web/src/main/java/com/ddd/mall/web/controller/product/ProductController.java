package com.ddd.mall.web.controller.product;

import com.ddd.mall.application.command.product.ChangePriceCommand;
import com.ddd.mall.application.command.product.CreateProductCommand;
import com.ddd.mall.application.command.product.ProductApplicationService;
import com.ddd.mall.application.command.product.PublishProductCommand;
import com.ddd.mall.application.query.product.ProductListQuery;
import com.ddd.mall.application.query.product.ProductQueryService;
import com.ddd.mall.application.query.product.ProductSearchQuery;
import com.ddd.mall.application.query.product.dto.ProductDetailDto;
import com.ddd.mall.application.query.product.dto.ProductListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.web.request.product.ChangePriceRequest;
import com.ddd.mall.web.request.product.CreateProductRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品接口
 * 提供商品管理和商品查询能力
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductApplicationService productApplicationService;
    private final ProductQueryService productQueryService;

    /**
     * 分页查询商品
     */
    @GetMapping
    public ApiResponse<PageResult<ProductListItemDto>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        ProductListQuery query = new ProductListQuery(categoryId, status);
        query.setPageNum(page);
        query.setPageSize(size);
        return ApiResponse.ok(productQueryService.productList(query));
    }

    /**
     * 查询商品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailDto> getProductDetail(@PathVariable Long id) {
        return ApiResponse.ok(productQueryService.productDetail(id));
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public ApiResponse<PageResult<ProductListItemDto>> searchProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String keyword) {
        ProductSearchQuery query = new ProductSearchQuery(keyword);
        query.setPageNum(page);
        query.setPageSize(size);
        return ApiResponse.ok(productQueryService.searchProducts(query));
    }

    /**
     * 创建商品
     */
    @PostMapping
    public ApiResponse<Long> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                request.getName(), request.getDescription(),
                request.getPrice(), request.getCategory());
        return ApiResponse.ok(productApplicationService.createProduct(command));
    }

    /**
     * 上架商品
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishProduct(@PathVariable Long id) {
        productApplicationService.publishProduct(new PublishProductCommand(id));
        return ApiResponse.ok();
    }

    /**
     * 修改商品价格
     */
    @PutMapping("/{id}/price")
    public ApiResponse<Void> changePrice(@PathVariable Long id,
                                         @Valid @RequestBody ChangePriceRequest request) {
        productApplicationService.changePrice(new ChangePriceCommand(id, request.getNewPrice()));
        return ApiResponse.ok();
    }
}