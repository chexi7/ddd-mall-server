package com.ddd.mall.domain.cart;

import com.ddd.mall.domain.shared.Entity;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 购物车项（实体，属于 Cart 聚合）
 */
@Getter
@Setter
public class CartItem extends Entity {

    private Long productId;
    private Long skuId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    protected CartItem() {}

    CartItem(Long productId, Long skuId, String productName, int quantity, BigDecimal unitPrice) {
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

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
