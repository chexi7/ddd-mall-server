package com.ddd.mall.domain.inventory;

import java.util.Optional;

/**
 * 库存仓储接口
 */
public interface InventoryRepository {

    Optional<Inventory> findByProductId(Long productId);

    void save(Inventory inventory);
}
