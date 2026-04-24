package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.shared.DomainEvent;
import com.ddd.mall.infrastructure.persistence.OrderJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.OrderConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.OrderDO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(OrderConverter::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return jpaRepository.findByOrderNo(orderNo).map(OrderConverter::toDomain);
    }

    @Override
    public List<Order> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId).stream()
                .map(OrderConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public void save(Order order) {
        OrderDO saved = jpaRepository.save(OrderConverter.toDO(order));
        order.setId(saved.getId());
        order.setVersion(saved.getVersion());
        List<DomainEvent> events = order.getDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
    }
}
