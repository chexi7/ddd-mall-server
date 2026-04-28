package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.ProductDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductDO, Long>, JpaSpecificationExecutor<ProductDO> {
    List<ProductDO> findByCategory(String category);
    List<ProductDO> findByStatus(String status);
}
