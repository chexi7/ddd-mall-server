package com.ddd.mall.web.controller.product;

import com.ddd.mall.domain.product.ProductStatus;
import com.ddd.mall.infrastructure.persistence.ProductJpaRepository;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductSkuDO;
import com.ddd.mall.web.response.ApiResponse;
import com.ddd.mall.web.response.product.CategoryView;
import com.ddd.mall.web.response.product.ProductSkuView;
import com.ddd.mall.web.response.product.ProductView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商城首页接口
 * 提供分类 推荐和热门商品查询能力
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StorefrontController {

    private final ProductJpaRepository productJpaRepository;

    /**
     * 查询分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryView>> categories() {
        List<CategoryView> categories = buildCategoryIdMap().entrySet().stream()
                .map(entry -> new CategoryView(entry.getValue(), entry.getKey(), null, "apps-o", List.of()))
                .toList();
        return ApiResponse.ok(categories);
    }

    /**
     * 查询推荐商品
     *
     * @return 推荐商品列表
     */
    @GetMapping("/home/recommend")
    public ApiResponse<List<ProductView>> recommendProducts() {
        List<ProductView> products = findOnSaleProducts().stream()
                .limit(6)
                .map(product -> toProductView(product, buildCategoryIdMap()))
                .toList();
        return ApiResponse.ok(products);
    }

    /**
     * 查询热门商品
     *
     * @return 热门商品列表
     */
    @GetMapping("/home/hot")
    public ApiResponse<List<ProductView>> hotProducts() {
        List<ProductView> products = findOnSaleProducts().stream()
                .sorted(Comparator.comparing(ProductDO::getPrice, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ProductDO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(product -> toProductView(product, buildCategoryIdMap()))
                .toList();
        return ApiResponse.ok(products);
    }

    private List<ProductDO> findOnSaleProducts() {
        return productJpaRepository.findByStatus(ProductStatus.ON_SALE.name()).stream()
                .sorted(Comparator.comparing(ProductDO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private Map<String, Long> buildCategoryIdMap() {
        List<String> categories = productJpaRepository.findAll().stream()
                .map(ProductDO::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .sorted()
                .collect(Collectors.toList());
        Map<String, Long> categoryIdMap = new LinkedHashMap<>();
        long id = 1L;
        for (String category : new LinkedHashSet<>(categories)) {
            categoryIdMap.put(category, id++);
        }
        return categoryIdMap;
    }

    private ProductView toProductView(ProductDO product, Map<String, Long> categoryIdMap) {
        List<ProductSkuView> skuViews = product.getSkus() == null ? List.of() : product.getSkus().stream()
                .map(this::toProductSkuView)
                .toList();
        return new ProductView(
                product.getId(),
                product.getName(),
                product.getDescription(),
                "",
                List.of(),
                mapStatus(product.getStatus()),
                skuViews,
                categoryIdMap.getOrDefault(product.getCategory(), 0L),
                product.getCreatedAt() == null ? null : product.getCreatedAt().toString()
        );
    }

    private ProductSkuView toProductSkuView(ProductSkuDO sku) {
        BigDecimal price = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
        return new ProductSkuView(
                sku.getId(),
                sku.getName() == null || sku.getName().isBlank() ? "SKU-" + sku.getId() : sku.getName(),
                parseAttributes(sku.getAttributes()),
                price,
                price,
                999
        );
    }

    private Map<String, String> parseAttributes(String rawAttributes) {
        if (rawAttributes == null || rawAttributes.isBlank()) {
            return Map.of();
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        String[] entries = rawAttributes.split("[,;；，]");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            String[] pair = trimmed.split("[:=：]", 2);
            if (pair.length == 2) {
                attributes.put(pair[0].trim(), pair[1].trim());
            } else {
                attributes.put("规格", trimmed);
            }
        }
        return attributes;
    }

    private String mapStatus(String status) {
        if (ProductStatus.ON_SALE.name().equals(status)) {
            return "PUBLISHED";
        }
        if (ProductStatus.OFF_SALE.name().equals(status)) {
            return "UNPUBLISHED";
        }
        return "DRAFT";
    }

}
