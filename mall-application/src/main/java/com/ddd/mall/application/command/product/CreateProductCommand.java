package com.ddd.mall.application.command.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class CreateProductCommand {

    /**
     * 商品名称
     */
    private final String name;

    /**
     * 商品描述
     */
    private final String description;

    /**
     * 商品价格
     */
    private final BigDecimal price;

    /**
     * 商品分类
     */
    private final String category;
}