package com.ddd.mall.infrastructure.persistence.query;

import com.ddd.mall.domain.member.query.MemberQueryPort;
import com.ddd.mall.infrastructure.persistence.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberQueryPortImpl implements MemberQueryPort {

    private final MemberJpaRepository jpaRepository;

    @Override
    public long countTotal() {
        return jpaRepository.count();
    }
}