package com.ddd.mall.infrastructure.persistence;

import com.ddd.mall.infrastructure.persistence.dataobject.MemberDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberDO, Long> {
    Optional<MemberDO> findByUsername(String username);
    boolean existsByUsername(String username);
}
