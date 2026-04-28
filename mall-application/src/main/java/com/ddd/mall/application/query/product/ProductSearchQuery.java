package com.ddd.mall.application.query.product;

import com.ddd.mall.application.query.support.PageQuery;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品搜索查询入参
 */
@Getter
@RequiredArgsConstructor
public class ProductSearchQuery extends PageQuery {

    /**
     * 搜索关键字
     */
    private final String keyword;
}