package com.ddd.mall.domain.cart;

import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 购物车聚合根
 */
@Getter
public class Cart extends AggregateRoot {

    @Setter private Long memberId;
    private final List<CartItem> items = new ArrayList<>();

    protected Cart() {}

    public Cart(Long memberId) {
        this.memberId = memberId;
    }

    public void addItem(Long productId, Long skuId, String productName, int quantity, java.math.BigDecimal unitPrice) {
        if (quantity <= 0) throw new DomainException("数量必须大于0");
        Optional<CartItem> existing = findItem(productId, skuId);
        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
        } else {
            items.add(new CartItem(productId, skuId, productName, quantity, unitPrice));
        }
    }

    public void changeQuantity(Long productId, Long skuId, int quantity) {
        CartItem item = findItem(productId, skuId)
                .orElseThrow(() -> new DomainException("购物车中不存在该商品"));
        if (quantity <= 0) { items.remove(item); } else { item.changeQuantity(quantity); }
    }

    public void removeItem(Long productId, Long skuId) {
        CartItem item = findItem(productId, skuId)
                .orElseThrow(() -> new DomainException("购物车中不存在该商品"));
        items.remove(item);
    }

    public void removeItemsByProductIds(List<Long> productIds) {
        items.removeIf(item -> productIds.contains(item.getProductId()));
    }

    public void clear() { items.clear(); }

    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }

    /** 仓储重建用 */
    public void addItemInternal(CartItem item) { this.items.add(item); }

    private Optional<CartItem> findItem(Long productId, Long skuId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId)
                        && (skuId == null ? i.getSkuId() == null : skuId.equals(i.getSkuId())))
                .findFirst();
    }
}
