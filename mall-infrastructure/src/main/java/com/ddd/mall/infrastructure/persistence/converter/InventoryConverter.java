package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.inventory.Inventory;
import com.ddd.mall.infrastructure.persistence.dataobject.InventoryDO;

public class InventoryConverter {

    public static Inventory toDomain(InventoryDO d) {
        Inventory inv = new Inventory(d.getProductId(), d.getTotalStock());
        inv.setId(d.getId());
        inv.setVersion(d.getVersion());
        // 覆盖构造函数的默认值
        inv.setAvailableStock(d.getAvailableStock());
        inv.setLockedStock(d.getLockedStock());
        inv.clearDomainEvents();
        return inv;
    }

    public static InventoryDO toDO(Inventory inv) {
        InventoryDO d = new InventoryDO();
        d.setId(inv.getId());
        d.setVersion(inv.getVersion());
        d.setProductId(inv.getProductId());
        d.setTotalStock(inv.getTotalStock());
        d.setAvailableStock(inv.getAvailableStock());
        d.setLockedStock(inv.getLockedStock());
        return d;
    }
}
