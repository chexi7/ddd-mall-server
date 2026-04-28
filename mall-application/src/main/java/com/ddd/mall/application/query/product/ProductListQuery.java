package com.ddd.mall.application.query.product;

import com.ddd.mall.application.query.support.PageQuery;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品列表查询入参
 */
@Getter
@RequiredArgsConstructor
public class ProductListQuery extends PageQuery {

    /**
     * 分类ID
     */
    private final Long categoryId;

    /**
     * 商品状态
     */
    private final String status;
}