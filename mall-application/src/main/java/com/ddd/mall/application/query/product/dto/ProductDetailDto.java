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

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品状态
     */
    private String status;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * SKU列表
     */
    private List<ProductSkuDto> skus;

    /**
     * 创建时间
     */
    private String createdAt;
}