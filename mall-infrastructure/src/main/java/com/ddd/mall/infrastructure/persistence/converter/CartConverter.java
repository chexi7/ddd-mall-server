package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.cart.Cart;
import com.ddd.mall.domain.cart.CartItem;
import com.ddd.mall.domain.shared.Money;
import com.ddd.mall.infrastructure.persistence.dataobject.CartDO;
import com.ddd.mall.infrastructure.persistence.dataobject.CartItemDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CartConverter {

    public static Cart toDomain(CartDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("memberId", d.getMemberId());

        Cart cart = DomainObjectReconstructor.reconstruct(Cart.class, fields);
        for (CartItemDO itemDO : d.getItems()) {
            cart.addItemInternal(toItemDomain(itemDO));
        }
        cart.clearDomainEvents();
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
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("productId", d.getProductId());
        fields.put("skuId", d.getSkuId());
        fields.put("productName", d.getProductName());
        fields.put("quantity", d.getQuantity());
        fields.put("unitPrice", Money.of(d.getUnitPrice()));
        return DomainObjectReconstructor.reconstruct(CartItem.class, fields);
    }

    private static CartItemDO toItemDO(CartItem item) {
        CartItemDO d = new CartItemDO();
        d.setId(item.getId());
        d.setProductId(item.getProductId());
        d.setSkuId(item.getSkuId());
        d.setProductName(item.getProductName());
        d.setQuantity(item.getQuantity());
        d.setUnitPrice(item.getUnitPrice().getAmount());
        return d;
    }
}