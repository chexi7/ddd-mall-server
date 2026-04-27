package com.ddd.mall.web.controller.product;

import com.ddd.mall.application.command.product.ChangePriceCommand;
import com.ddd.mall.application.command.product.CreateProductCommand;
import com.ddd.mall.application.command.product.ProductApplicationService;
import com.ddd.mall.application.command.product.PublishProductCommand;
import com.ddd.mall.domain.product.ProductStatus;
import com.ddd.mall.infrastructure.persistence.ProductJpaRepository;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductSkuDO;
import com.ddd.mall.web.request.product.ChangePriceRequest;
import com.ddd.mall.web.request.product.CreateProductRequest;
import com.ddd.mall.web.response.ApiResponse;
import com.ddd.mall.web.response.PageResponse;
import com.ddd.mall.web.response.product.ProductSkuView;
import com.ddd.mall.web.response.product.ProductView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品接口
 * 提供商品管理和商品查询能力
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductApplicationService productApplicationService;
    private final ProductJpaRepository productJpaRepository;

    /**
     * 分页查询商品
     *
     * @param page 页码
     * @param size 每页条数
     * @param categoryId 分类ID
     * @param status 商品状态
     * @return 分页商品列表
     */
    @GetMapping
    public ApiResponse<PageResponse<ProductView>> listProducts(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(required = false) Long categoryId,
                                                               @RequestParam(required = false) String status) {
        List<ProductDO> filteredProducts = filterProducts(categoryId, status, null);
        return ApiResponse.ok(toPageResponse(filteredProducts, page, size));
    }

    /**
     * 查询商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductView> getProductDetail(@PathVariable Long id) {
        ProductDO product = productJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        return ApiResponse.ok(toProductView(product, buildCategoryIdMap()));
    }

    /**
     * 搜索商品
     *
     * @param page 页码
     * @param size 每页条数
     * @param keyword 搜索关键字
     * @return 分页商品列表
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductView>> searchProducts(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam String keyword) {
        List<ProductDO> filteredProducts = filterProducts(null, null, keyword);
        return ApiResponse.ok(toPageResponse(filteredProducts, page, size));
    }

    /**
     * 创建商品
     *
     * @param request 创建商品请求参数
     * @return 创建的商品ID
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
     *
     * @param id 商品ID
     * @return 空响应
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishProduct(@PathVariable Long id) {
        productApplicationService.publishProduct(new PublishProductCommand(id));
        return ApiResponse.ok();
    }

    /**
     * 修改商品价格
     *
     * @param id 商品ID
     * @param request 修改价格请求参数
     * @return 空响应
     */
    @PutMapping("/{id}/price")
    public ApiResponse<Void> changePrice(@PathVariable Long id,
                                         @Valid @RequestBody ChangePriceRequest request) {
        productApplicationService.changePrice(new ChangePriceCommand(id, request.getNewPrice()));
        return ApiResponse.ok();
    }

    private List<ProductDO> filterProducts(Long categoryId, String status, String keyword) {
        Map<String, Long> categoryIdMap = buildCategoryIdMap();
        return productJpaRepository.findAll().stream()
                .filter(product -> isVisibleStatus(product, status))
                .filter(product -> categoryId == null || Objects.equals(categoryIdMap.get(product.getCategory()), categoryId))
                .filter(product -> matchesKeyword(product, keyword))
                .sorted(Comparator.comparing(ProductDO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private boolean isVisibleStatus(ProductDO product, String status) {
        if (status != null && !status.isBlank()) {
            return mapStatus(product.getStatus()).equalsIgnoreCase(status);
        }
        return ProductStatus.ON_SALE.name().equals(product.getStatus());
    }

    private boolean matchesKeyword(ProductDO product, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        return (product.getName() != null && product.getName().toLowerCase().contains(normalizedKeyword))
                || (product.getDescription() != null && product.getDescription().toLowerCase().contains(normalizedKeyword));
    }

    private PageResponse<ProductView> toPageResponse(List<ProductDO> products, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min((safePage - 1) * safeSize, products.size());
        int toIndex = Math.min(fromIndex + safeSize, products.size());
        Map<String, Long> categoryIdMap = buildCategoryIdMap();
        List<ProductView> content = products.subList(fromIndex, toIndex).stream()
                .map(product -> toProductView(product, categoryIdMap))
                .toList();
        int totalPages = (int) Math.ceil((double) products.size() / safeSize);
        return new PageResponse<>(content, (long) products.size(), totalPages, safePage, safeSize);
    }

    private Map<String, Long> buildCategoryIdMap() {
        List<String> categories = productJpaRepository.findAll().stream()
                .map(ProductDO::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
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