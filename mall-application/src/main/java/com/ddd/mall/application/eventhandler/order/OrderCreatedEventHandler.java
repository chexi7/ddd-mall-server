package com.ddd.mall.application.eventhandler.order;

import com.ddd.mall.domain.cart.CartRepository;
import com.ddd.mall.domain.inventory.Inventory;
import com.ddd.mall.domain.inventory.InventoryRepository;
import com.ddd.mall.domain.order.event.OrderCreatedEvent;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventHandler {

    private final InventoryRepository inventoryRepository;
    private final CartRepository cartRepository;

    @EventListener
    @Transactional
    public void handle(OrderCreatedEvent event) {
        log.info("处理订单创建事件: orderNo={}", event.getOrderNo());

        for (OrderCreatedEvent.OrderItemInfo item : event.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new DomainException("库存记录不存在: productId=" + item.getProductId()));
            inventory.lock(item.getQuantity());
            inventoryRepository.save(inventory);
        }

        cartRepository.findByMemberId(event.getMemberId()).ifPresent(cart -> {
            List<Long> productIds = event.getItems().stream()
                    .map(OrderCreatedEvent.OrderItemInfo::getProductId)
                    .collect(Collectors.toList());
            cart.removeItemsByProductIds(productIds);
            cartRepository.save(cart);
        });
    }
}
