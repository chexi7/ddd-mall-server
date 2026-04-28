package com.ddd.mall.application.query.product;

import com.ddd.mall.application.query.product.dto.CategoryDto;
import com.ddd.mall.application.query.product.dto.ProductDetailDto;
import com.ddd.mall.application.query.product.dto.ProductListItemDto;
import com.ddd.mall.application.query.product.dto.ProductSkuDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductRepository;
import com.ddd.mall.domain.product.ProductSku;
import com.ddd.mall.domain.product.ProductStatus;
import com.ddd.mall.domain.product.query.ProductPageResult;
import com.ddd.mall.domain.product.query.ProductQueryPort;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品聚合查询服务。
 * <p>
 * CQRS 模式：写操作通过 ProductRepository，读操作通过 ProductQueryPort。
 */
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductQueryPort productQueryPort;

    /**
     * 商品分页列表（管理端）
     */
    @Transactional(readOnly = true)
    public PageResult<ProductListItemDto> productList(ProductListQuery query) {
        int safePage = Math.max(query.getPageNum(), 1);
        int safeSize = Math.max(query.getPageSize(), 10);

        String category = resolveCategoryName(query.getCategoryId());
        String statusApi = query.getStatus();

        ProductPageResult slice = productQueryPort.findPage(safePage, safeSize, statusApi, category, null);
        Map<String, Long> categoryIdMap = buildCategoryIdMap();

        List<ProductListItemDto> content = slice.getContent().stream()
                .map(p -> toListItemDto(p, categoryIdMap))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) slice.getTotalElements() / safeSize);
        return PageResult.<ProductListItemDto>builder()
                .data(content)
                .totalCount(slice.getTotalElements())
                .totalPages(totalPages)
                .pageNum(safePage)
                .pageSize(safeSize)
                .build();
    }

    /**
     * 商品搜索（C端）
     */
    @Transactional(readOnly = true)
    public PageResult<ProductListItemDto> searchProducts(ProductSearchQuery query) {
        int safePage = Math.max(query.getPageNum(), 1);
        int safeSize = Math.max(query.getPageSize(), 10);

        ProductPageResult slice = productQueryPort.findPage(safePage, safeSize, "PUBLISHED", null, query.getKeyword());
        Map<String, Long> categoryIdMap = buildCategoryIdMap();

        List<ProductListItemDto> content = slice.getContent().stream()
                .map(p -> toListItemDto(p, categoryIdMap))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) slice.getTotalElements() / safeSize);
        return PageResult.<ProductListItemDto>builder()
                .data(content)
                .totalCount(slice.getTotalElements())
                .totalPages(totalPages)
                .pageNum(safePage)
                .pageSize(safeSize)
                .build();
    }

    /**
     * 商品详情
     */
    @Transactional(readOnly = true)
    public ProductDetailDto productDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainException("商品不存在: " + productId));
        Map<String, Long> categoryIdMap = buildCategoryIdMap();
        return toDetailDto(product, categoryIdMap);
    }

    /**
     * 推荐商品（C端首页，限6个）
     */
    @Transactional(readOnly = true)
    public List<ProductListItemDto> recommendProducts() {
        Map<String, Long> categoryIdMap = buildCategoryIdMap();
        return productQueryPort.findOnSaleProductsOrdered().stream()
                .limit(6)
                .map(p -> toListItemDto(p, categoryIdMap))
                .collect(Collectors.toList());
    }

    /**
     * 热门商品（C端首页，限6个，按价格倒序）
     */
    @Transactional(readOnly = true)
    public List<ProductListItemDto> hotProducts() {
        Map<String, Long> categoryIdMap = buildCategoryIdMap();
        return productQueryPort.findOnSaleProductsOrdered().stream()
                .sorted(Comparator.comparing((Product p) -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice().getAmount(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(p -> toListItemDto(p, categoryIdMap))
                .collect(Collectors.toList());
    }

    /**
     * 分类列表（C端）
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> categories() {
        Map<String, Long> categoryIdMap = buildCategoryIdMap();
        return categoryIdMap.entrySet().stream()
                .map(entry -> CategoryDto.builder()
                        .id(entry.getValue())
                        .name(entry.getKey())
                        .parentId(null)
                        .icon("apps-o")
                        .children(List.of())
                        .build())
                .collect(Collectors.toList());
    }

    // ── 私有辅助方法 ──────────────────────────────────

    private ProductListItemDto toListItemDto(Product product, Map<String, Long> categoryIdMap) {
        List<ProductSkuDto> skuDtos = product.getSkus().stream()
                .map(this::toSkuDto)
                .collect(Collectors.toList());

        return ProductListItemDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice() == null ? BigDecimal.ZERO : product.getPrice().getAmount())
                .status(mapStatus(product.getStatus()))
                .categoryId(categoryIdMap.getOrDefault(product.getCategory(), 0L))
                .skus(skuDtos)
                .createdAt(product.getCreatedAt() == null ? null : product.getCreatedAt().toString())
                .build();
    }

    private ProductDetailDto toDetailDto(Product product, Map<String, Long> categoryIdMap) {
        List<ProductSkuDto> skuDtos = product.getSkus().stream()
                .map(this::toSkuDto)
                .collect(Collectors.toList());

        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice() == null ? BigDecimal.ZERO : product.getPrice().getAmount())
                .status(mapStatus(product.getStatus()))
                .categoryId(categoryIdMap.getOrDefault(product.getCategory(), 0L))
                .skus(skuDtos)
                .createdAt(product.getCreatedAt() == null ? null : product.getCreatedAt().toString())
                .build();
    }

    private ProductSkuDto toSkuDto(ProductSku sku) {
        BigDecimal price = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice().getAmount();
        return ProductSkuDto.builder()
                .id(sku.getId())
                .skuCode(sku.getName() == null || sku.getName().isBlank() ? "SKU-" + sku.getId() : sku.getName())
                .attributes(parseAttributes(sku.getAttributes()))
                .price(price)
                .originalPrice(price)
                .stock(999)
                .build();
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

    private String mapStatus(ProductStatus status) {
        if (status == null) {
            return "DRAFT";
        }
        return switch (status) {
            case ON_SALE -> "PUBLISHED";
            case OFF_SALE -> "UNPUBLISHED";
            default -> "DRAFT";
        };
    }

    /**
     * 构建 categoryName → categoryId 的映射。
     */
    private Map<String, Long> buildCategoryIdMap() {
        List<String> categories = productQueryPort.findAllCategories();
        Map<String, Long> categoryIdMap = new LinkedHashMap<>();
        long id = 1L;
        for (String category : new LinkedHashSet<>(categories)) {
            categoryIdMap.put(category, id++);
        }
        return categoryIdMap;
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Map<String, Long> categoryIdMap = buildCategoryIdMap();
        for (Map.Entry<String, Long> entry : categoryIdMap.entrySet()) {
            if (entry.getValue().equals(categoryId)) {
                return entry.getKey();
            }
        }
        return null;
    }
}