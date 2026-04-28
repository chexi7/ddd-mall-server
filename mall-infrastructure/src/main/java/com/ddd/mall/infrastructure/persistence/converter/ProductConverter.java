package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductSku;
import com.ddd.mall.domain.product.ProductStatus;
import com.ddd.mall.domain.shared.Money;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import com.ddd.mall.infrastructure.persistence.dataobject.ProductSkuDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductConverter {

    public static Product toDomain(ProductDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("name", d.getName());
        fields.put("description", d.getDescription());
        fields.put("price", Money.of(d.getPrice()));
        fields.put("status", ProductStatus.valueOf(d.getStatus()));
        fields.put("category", d.getCategory());
        fields.put("createdAt", d.getCreatedAt());
        fields.put("updatedAt", d.getUpdatedAt());

        Product p = DomainObjectReconstructor.reconstruct(Product.class, fields);
        for (ProductSkuDO skuDO : d.getSkus()) {
            p.addSkuInternal(toSkuDomain(skuDO));
        }
        p.clearDomainEvents();
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
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("name", d.getName());
        fields.put("price", Money.of(d.getPrice()));
        fields.put("attributes", d.getAttributes());
        return DomainObjectReconstructor.reconstruct(ProductSku.class, fields);
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