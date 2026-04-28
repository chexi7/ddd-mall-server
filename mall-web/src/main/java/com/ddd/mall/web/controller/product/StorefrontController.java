package com.ddd.mall.web.controller.product;

import com.ddd.mall.application.query.product.ProductQueryService;
import com.ddd.mall.application.query.product.dto.CategoryDto;
import com.ddd.mall.application.query.product.dto.ProductListItemDto;
import com.ddd.mall.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商城首页接口
 * 提供分类、推荐和热门商品查询能力
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StorefrontController {

    private final ProductQueryService productQueryService;

    /**
     * 查询分类列表
     */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> categories() {
        return ApiResponse.ok(productQueryService.categories());
    }

    /**
     * 查询推荐商品
     */
    @GetMapping("/home/recommend")
    public ApiResponse<List<ProductListItemDto>> recommendProducts() {
        return ApiResponse.ok(productQueryService.recommendProducts());
    }

    /**
     * 查询热门商品
     */
    @GetMapping("/home/hot")
    public ApiResponse<List<ProductListItemDto>> hotProducts() {
        return ApiResponse.ok(productQueryService.hotProducts());
    }
}