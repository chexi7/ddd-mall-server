package com.ddd.mall.domain.product;

import com.ddd.mall.domain.product.event.ProductCreatedEvent;
import com.ddd.mall.domain.product.event.ProductPriceChangedEvent;
import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商品聚合根
 */
@Getter
public class Product extends AggregateRoot {

    @Setter private String name;
    @Setter private String description;
    @Setter private Money price;
    @Setter private ProductStatus status;
    @Setter private String category;
    private final List<ProductSku> skus = new ArrayList<>();
    @Setter private LocalDateTime createdAt;
    @Setter private LocalDateTime updatedAt;

    protected Product() {}

    public Product(String name, String description, Money price, String category) {
        if (name == null || name.isBlank()) throw new DomainException("商品名称不能为空");
        if (price == null) throw new DomainException("商品价格不能为空");
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = ProductStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        registerEvent(new ProductCreatedEvent(this));
    }

    public void publish() {
        if (this.status == ProductStatus.ON_SALE) throw new DomainException("商品已在售，无需重复上架");
        if (this.skus.isEmpty()) throw new DomainException("商品至少需要一个 SKU 才能上架");
        this.status = ProductStatus.ON_SALE;
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw() {
        if (this.status != ProductStatus.ON_SALE) throw new DomainException("只有在售商品才能下架");
        this.status = ProductStatus.OFF_SALE;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePrice(Money newPrice) {
        if (newPrice == null) throw new DomainException("价格不能为空");
        Money oldPrice = this.price;
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new ProductPriceChangedEvent(this.getId(), oldPrice, newPrice));
    }

    public void addSku(String skuName, Money skuPrice, String attributes) {
        this.skus.add(new ProductSku(skuName, skuPrice, attributes));
        this.updatedAt = LocalDateTime.now();
    }

    public List<ProductSku> getSkus() { return Collections.unmodifiableList(skus); }
    public boolean isOnSale() { return this.status == ProductStatus.ON_SALE; }

    /** 仓储重建用 */
    public void addSkuInternal(ProductSku sku) { this.skus.add(sku); }
}
