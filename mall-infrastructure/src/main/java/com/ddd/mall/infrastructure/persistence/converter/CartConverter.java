package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.cart.Cart;
import com.ddd.mall.domain.cart.CartItem;
import com.ddd.mall.infrastructure.persistence.dataobject.CartDO;
import com.ddd.mall.infrastructure.persistence.dataobject.CartItemDO;

import java.util.stream.Collectors;

public class CartConverter {

    public static Cart toDomain(CartDO d) {
        Cart cart = new Cart(d.getMemberId());
        cart.setId(d.getId());
        cart.setVersion(d.getVersion());
        cart.clearDomainEvents();
        for (CartItemDO itemDO : d.getItems()) {
            CartItem item = toItemDomain(itemDO);
            cart.addItemInternal(item);
        }
        return cart;
    }

    public static CartDO toDO(Cart cart) {
        CartDO d = new CartDO();
        d.setId(cart.getId());
        d.setVersion(cart.getVersion());
        d.setMemberId(cart.getMemberId());
        d.setItems(cart.getItems().stream().map(CartConverter::toItemDO).collect(Collectors.toList()));
        return d;
    }

    private static CartItem toItemDomain(CartItemDO d) {
        CartItem item = new CartItem() {};
        item.setId(d.getId());
        item.setProductId(d.getProductId());
        item.setSkuId(d.getSkuId());
        item.setProductName(d.getProductName());
        item.setQuantity(d.getQuantity());
        item.setUnitPrice(d.getUnitPrice());
        return item;
    }

    private static CartItemDO toItemDO(CartItem item) {
        CartItemDO d = new CartItemDO();
        d.setId(item.getId());
        d.setProductId(item.getProductId());
        d.setSkuId(item.getSkuId());
        d.setProductName(item.getProductName());
        d.setQuantity(item.getQuantity());
        d.setUnitPrice(item.getUnitPrice());
        return d;
    }
}
