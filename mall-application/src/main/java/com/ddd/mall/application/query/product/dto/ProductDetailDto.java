package com.ddd.mall.application.query.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情（管理端 / C端共用）
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String status;
    private Long categoryId;
    private List<ProductSkuDto> skus;
    private String createdAt;
}