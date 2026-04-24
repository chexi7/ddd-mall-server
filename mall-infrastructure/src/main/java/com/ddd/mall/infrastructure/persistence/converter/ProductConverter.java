package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductSku;
import com.ddd.mall.domain.product.ProductStatus;
import com.ddd.mall.domain.shared.Money;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductSkuDO;

import java.util.stream.Collectors;

public class ProductConverter {

    public static Product toDomain(ProductDO d) {
        Product p = new Product(d.getName(), d.getDescription(), Money.of(d.getPrice()), d.getCategory());
        p.setId(d.getId());
        p.setVersion(d.getVersion());
        // 覆盖构造函数中设置的默认值
        p.setStatus(ProductStatus.valueOf(d.getStatus()));
        p.setCreatedAt(d.getCreatedAt());
        p.setUpdatedAt(d.getUpdatedAt());
        // 清掉构造函数中注册的事件（这是重建，不是新建）
        p.clearDomainEvents();
        for (ProductSkuDO skuDO : d.getSkus()) {
            ProductSku sku = toSkuDomain(skuDO);
            p.addSkuInternal(sku);
        }
        return p;
    }

    public static ProductDO toDO(Product p) {
        ProductDO d = new ProductDO();
        d.setId(p.getId());
        d.setVersion(p.getVersion());
        d.setName(p.getName());
        d.setDescription(p.getDescription());
        d.setPrice(p.getPrice().getAmount());
        d.setStatus(p.getStatus().name());
        d.setCategory(p.getCategory());
        d.setCreatedAt(p.getCreatedAt());
        d.setUpdatedAt(p.getUpdatedAt());
        d.setSkus(p.getSkus().stream().map(ProductConverter::toSkuDO).collect(Collectors.toList()));
        return d;
    }

    private static ProductSku toSkuDomain(ProductSkuDO d) {
        // 使用包级 setter 重建
        ProductSku sku = new ProductSku() {};
        sku.setId(d.getId());
        sku.setName(d.getName());
        sku.setPrice(Money.of(d.getPrice()));
        sku.setAttributes(d.getAttributes());
        return sku;
    }

    private static ProductSkuDO toSkuDO(ProductSku s) {
        ProductSkuDO d = new ProductSkuDO();
        d.setId(s.getId());
        d.setName(s.getName());
        d.setPrice(s.getPrice().getAmount());
        d.setAttributes(s.getAttributes());
        return d;
    }
}
