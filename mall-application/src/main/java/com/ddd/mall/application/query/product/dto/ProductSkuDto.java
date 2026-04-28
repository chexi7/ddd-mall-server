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

    /**
     * SKU ID
     */
    private Long id;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU属性
     */
    private java.util.Map<String, String> attributes;

    /**
     * SKU价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    private Integer stock;
}