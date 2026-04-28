package com.ddd.mall.domain.product;

import java.util.Optional;

/**
 * 商品仓储接口（命令侧）
 */
public interface ProductRepository {

    Optional<Product> findById(Long id);

    void save(Product product);

    void remove(Product product);
}