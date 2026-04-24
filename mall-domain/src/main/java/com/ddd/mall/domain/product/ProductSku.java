package com.ddd.mall.domain.product;

import com.ddd.mall.domain.shared.Entity;
import com.ddd.mall.domain.shared.Money;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品 SKU（实体，属于 Product 聚合）
 */
@Getter
@Setter
public class ProductSku extends Entity {

    private String name;
    private Money price;
    private String attributes;

    protected ProductSku() {}

    ProductSku(String name, Money price, String attributes) {
        this.name = name;
        this.price = price;
        this.attributes = attributes;
    }
}
