package com.ddd.mall.application.eventhandler.order;

import com.ddd.mall.domain.inventory.Inventory;
import com.ddd.mall.domain.inventory.InventoryRepository;
import com.ddd.mall.domain.order.event.OrderCancelledEvent;
import com.ddd.mall.domain.order.event.OrderCreatedEvent;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledEventHandler {

    private final InventoryRepository inventoryRepository;

    @EventListener
    @Transactional
    public void handle(OrderCancelledEvent event) {
        log.info("处理订单取消事件: orderNo={}", event.getOrderNo());

        for (OrderCreatedEvent.OrderItemInfo item : event.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new DomainException("库存记录不存在: productId=" + item.getProductId()));
            inventory.unlock(item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }
}
