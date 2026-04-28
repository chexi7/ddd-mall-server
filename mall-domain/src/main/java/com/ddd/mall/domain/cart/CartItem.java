package com.ddd.mall.domain.cart;

import com.ddd.mall.domain.shared.Entity;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import lombok.Getter;
import lombok.Setter;

/**
 * 购物车项（实体，属于 Cart 聚合）
 */
@Getter
@Setter
public class CartItem extends Entity {

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
     * 数量
     */
    private int quantity;

    /**
     * 单价
     */
    private Money unitPrice;

    protected CartItem() {}

    CartItem(Long productId, Long skuId, String productName, int quantity, Money unitPrice) {
        this.productId = productId;
        this.skuId = skuId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void increaseQuantity(int amount) { this.quantity += amount; }

    void changeQuantity(int newQuantity) {
        if (newQuantity <= 0) throw new DomainException("数量必须大于0");
        this.quantity = newQuantity;
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}