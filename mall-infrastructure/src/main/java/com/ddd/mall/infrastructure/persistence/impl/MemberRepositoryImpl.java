package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.member.Member;
import com.ddd.mall.domain.member.MemberRepository;
import com.ddd.mall.infrastructure.persistence.MemberJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.MemberConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.MemberDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository jpaRepository;

    @Override
    public Optional<Member> findById(Long id) {
        return jpaRepository.findById(id).map(MemberConverter::toDomain);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(MemberConverter::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public void save(Member member) {
        MemberDO saved = jpaRepository.save(MemberConverter.toDO(member));
        member.setId(saved.getId());
        member.setVersion(saved.getVersion());
    }
}