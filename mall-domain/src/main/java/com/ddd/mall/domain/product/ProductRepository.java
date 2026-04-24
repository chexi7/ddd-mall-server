package com.ddd.mall.domain.product;

import java.util.List;
import java.util.Optional;

/**
 * 商品仓储接口（领域层定义，基础设施层实现）
 */
public interface ProductRepository {

    Optional<Product> findById(Long id);

    List<Product> findByCategory(String category);

    List<Product> findOnSaleProducts();

    void save(Product product);

    void remove(Product product);
}
