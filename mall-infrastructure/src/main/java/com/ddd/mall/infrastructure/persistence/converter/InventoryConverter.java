package com.ddd.mall.infrastructure.persistence.converter;

import com.ddd.mall.domain.inventory.Inventory;
import com.ddd.mall.infrastructure.persistence.dataobject.InventoryDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;

import java.util.LinkedHashMap;
import java.util.Map;

public class InventoryConverter {

    public static Inventory toDomain(InventoryDO d) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", d.getId());
        fields.put("version", d.getVersion());
        fields.put("productId", d.getProductId());
        fields.put("totalStock", d.getTotalStock());
        fields.put("availableStock", d.getAvailableStock());
        fields.put("lockedStock", d.getLockedStock());

        Inventory inv = DomainObjectReconstructor.reconstruct(Inventory.class, fields);
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