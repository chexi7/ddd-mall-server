package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.OrderPageSlice;
import com.ddd.mall.domain.order.OrderRepository;
import com.ddd.mall.domain.shared.DomainEvent;
import com.ddd.mall.infrastructure.persistence.OrderJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.OrderConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.OrderDO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    public OrderPageSlice findPageForAdmin(int page, int size, String statusApi, String orderNoKeyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        String domainStatus = toDomainStatus(statusApi);
        String keyword = orderNoKeyword == null ? null : orderNoKeyword.trim();

        PageRequest pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<OrderDO> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (domainStatus != null && !domainStatus.isBlank()) {
                preds.add(cb.equal(root.get("status"), domainStatus));
            }
            if (keyword != null && !keyword.isBlank()) {
                preds.add(cb.like(root.get("orderNo"), "%" + keyword + "%"));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(Predicate[]::new));
        };
        Page<OrderDO> result = jpaRepository.findAll(spec, pageable);
        List<Order> content = result.getContent().stream().map(OrderConverter::toDomain).collect(Collectors.toList());
        return new OrderPageSlice(content, result.getTotalElements());
    }

    private static String toDomainStatus(String statusApi) {
        if (statusApi == null || statusApi.isBlank()) {
            return null;
        }
        return switch (statusApi) {
            case "CREATED" -> "PENDING_PAYMENT";
            case "DELIVERED" -> "COMPLETED";
            default -> statusApi;
        };
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
