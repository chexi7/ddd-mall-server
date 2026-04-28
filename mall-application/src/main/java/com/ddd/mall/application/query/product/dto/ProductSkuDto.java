package com.ddd.mall.application.query.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品 SKU 读模型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuDto {
    private Long id;
    private String skuCode;
    private java.util.Map<String, String> attributes;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
}