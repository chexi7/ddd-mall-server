package com.ddd.mall.infrastructure.persistence.impl;

import com.ddd.mall.domain.cart.Cart;
import com.ddd.mall.domain.cart.CartRepository;
import com.ddd.mall.infrastructure.persistence.CartJpaRepository;
import com.ddd.mall.infrastructure.persistence.converter.CartConverter;
import com.ddd.mall.infrastructure.persistence.dataobject.CartDO;
import com.ddd.mall.infrastructure.persistence.reflect.DomainObjectReconstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository jpaRepository;

    @Override
    public Optional<Cart> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId).map(CartConverter::toDomain);
    }

    @Override
    public void save(Cart cart) {
        CartDO saved = jpaRepository.save(CartConverter.toDO(cart));
        DomainObjectReconstructor.setIdAndVersion(cart, saved.getId(), saved.getVersion());
    }
}
