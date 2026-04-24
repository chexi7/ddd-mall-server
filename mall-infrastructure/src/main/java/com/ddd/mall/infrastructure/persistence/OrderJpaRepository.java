package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.OrderDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderDO, Long> {
    Optional<OrderDO> findByOrderNo(String orderNo);
    List<OrderDO> findByMemberId(Long memberId);
}
