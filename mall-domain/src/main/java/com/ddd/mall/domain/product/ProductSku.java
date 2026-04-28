package com.ddd.mall.domain.product;

import com.ddd.mall.domain.shared.Entity;
import com.ddd.mall.domain.shared.Money;
import com.ddd.mall.domain.shared.ReconstructionOnly;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 商品 SKU（实体，属于 Product 聚合）
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ReconstructionOnly
public class ProductSku extends Entity {

    /**
     * SKU名称
     */
    private String name;

    /**
     * SKU价格
     */
    private Money price;

    /**
     * SKU属性
     */
    private String attributes;

    ProductSku(String name, Money price, String attributes) {
        this.name = name;
        this.price = price;
        this.attributes = attributes;
    }
}