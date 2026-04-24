package com.ddd.mall.domain.inventory;

import com.ddd.mall.domain.inventory.event.InventoryDeductedEvent;
import com.ddd.mall.domain.inventory.event.InventoryRestoredEvent;
import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存聚合根
 */
@Getter
@Setter
public class Inventory extends AggregateRoot {

    private Long productId;
    private int totalStock;
    private int availableStock;
    private int lockedStock;

    protected Inventory() {}

    public Inventory(Long productId, int totalStock) {
        if (totalStock < 0) throw new DomainException("库存不能为负数");
        this.productId = productId;
        this.totalStock = totalStock;
        this.availableStock = totalStock;
        this.lockedStock = 0;
    }

    public void lock(int quantity) {
        if (quantity <= 0) throw new DomainException("预占数量必须大于0");
        if (this.availableStock < quantity)
            throw new DomainException("库存不足，当前可用: " + this.availableStock + ", 需要: " + quantity);
        this.availableStock -= quantity;
        this.lockedStock += quantity;
    }

    public void deduct(int quantity) {
        if (quantity <= 0) throw new DomainException("扣减数量必须大于0");
        if (this.lockedStock < quantity) throw new DomainException("锁定库存不足以扣减");
        this.lockedStock -= quantity;
        this.totalStock -= quantity;
        registerEvent(new InventoryDeductedEvent(this.productId, quantity));
    }

    public void unlock(int quantity) {
        if (quantity <= 0) throw new DomainException("释放数量必须大于0");
        this.lockedStock -= quantity;
        this.availableStock += quantity;
        registerEvent(new InventoryRestoredEvent(this.productId, quantity));
    }

    public void restock(int quantity) {
        if (quantity <= 0) throw new DomainException("补货数量必须大于0");
        this.totalStock += quantity;
        this.availableStock += quantity;
    }
}
