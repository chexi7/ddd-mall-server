package com.ddd.mall.domain.product;

import com.ddd.mall.domain.product.event.ProductCreatedEvent;
import com.ddd.mall.domain.product.event.ProductPriceChangedEvent;
import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import com.ddd.mall.domain.shared.ReconstructionOnly;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商品聚合根
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ReconstructionOnly
public class Product extends AggregateRoot {

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private Money price;

    /**
     * 商品状态
     */
    private ProductStatus status;

    /**
     * 商品分类
     */
    private String category;

    /**
     * SKU列表
     */
    private final List<ProductSku> skus = new ArrayList<>();

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

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

    /**
     * 仓储重建用
     */
    public void addSkuInternal(ProductSku sku) { this.skus.add(sku); }
}