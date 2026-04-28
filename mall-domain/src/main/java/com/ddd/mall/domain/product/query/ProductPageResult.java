package com.ddd.mall.domain.product.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 商品分页查询结果（读模型）
 */
@Getter
@RequiredArgsConstructor
public class ProductPageResult {

    /**
     * 商品列表
     */
    private final List<com.ddd.mall.domain.product.Product> content;

    /**
     * 总记录数
     */
    private final long totalElements;
}