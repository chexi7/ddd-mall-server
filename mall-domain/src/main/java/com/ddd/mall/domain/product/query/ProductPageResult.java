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
    private final List<com.ddd.mall.domain.product.Product> content;
    private final long totalElements;
}