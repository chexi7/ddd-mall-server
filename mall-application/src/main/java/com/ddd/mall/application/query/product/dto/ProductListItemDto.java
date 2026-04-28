package com.ddd.mall.application.query.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品列表项读模型（分页列表 / 推荐 / 热门共用）
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String status;
    private Long categoryId;
    private List<ProductSkuDto> skus;
    private String createdAt;
}