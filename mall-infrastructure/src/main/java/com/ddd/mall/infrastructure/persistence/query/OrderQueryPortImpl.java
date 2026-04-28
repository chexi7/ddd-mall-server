package com.ddd.mall.infrastructure.persistence.query;

import com.ddd.mall.domain.order.Order;
import com.ddd.mall.domain.order.query.OrderPageResult;
import com.ddd.mall.domain.order.query.OrderQueryPort;
import com.ddd.mall.infrastructure.persistence.OrderJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.OrderConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.OrderDO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryPortImpl implements OrderQueryPort {

    private final OrderJpaRepository jpaRepository;

    @Override
    public OrderPageResult findPageForAdmin(int page, int size, String statusApi, String orderNoKeyword) {
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
        return new OrderPageResult(content, result.getTotalElements());
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
    public long countTotal() {
        return jpaRepository.count();
    }

    @Override
    public long countByCreatedAtSince(LocalDateTime since) {
        return jpaRepository.countByCreatedAtGreaterThanEqual(since);
    }

    @Override
    public BigDecimal sumTotalAmountSince(LocalDateTime since) {
        return jpaRepository.sumTotalAmountSince(since);
    }
}