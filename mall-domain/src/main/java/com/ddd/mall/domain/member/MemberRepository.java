package com.ddd.mall.domain.member;

import java.util.Optional;

/**
 * 会员仓储接口
 */
public interface MemberRepository {

    Optional<Member> findById(Long id);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    void save(Member member);
}
