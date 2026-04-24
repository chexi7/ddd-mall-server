package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductDO, Long> {
    List<ProductDO> findByCategory(String category);
    List<ProductDO> findByStatus(String status);
}
