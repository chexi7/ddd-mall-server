package com.ddd.mall.domain.order;

import com.ddd.mall.domain.shared.Entity;
import com.ddd.mall.domain.shared.Money;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单项（实体，属于 Order 聚合）
 */
@Getter
@Setter
public class OrderItem extends Entity {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 单价
     */
    private Money unitPrice;

    /**
     * 数量
     */
    private int quantity;

    protected OrderItem() {}

    OrderItem(Long productId, Long skuId, String productName, Money unitPrice, int quantity) {
        this.productId = productId;
        this.skuId = skuId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}