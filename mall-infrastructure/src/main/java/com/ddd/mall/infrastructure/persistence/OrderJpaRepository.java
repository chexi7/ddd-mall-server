package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.OrderDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderDO, Long>, JpaSpecificationExecutor<OrderDO> {
    Optional<OrderDO> findByOrderNo(String orderNo);
    List<OrderDO> findByMemberId(Long memberId);

    long countByCreatedAtGreaterThanEqual(LocalDateTime start);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderDO o WHERE o.createdAt >= :start")
    BigDecimal sumTotalAmountSince(@Param("start") LocalDateTime start);
}
