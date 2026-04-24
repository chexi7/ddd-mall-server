package com.ddd.mall.domain.cart;

import java.util.Optional;

/**
 * 购物车仓储接口
 */
public interface CartRepository {

    Optional<Cart> findByMemberId(Long memberId);

    void save(Cart cart);
}
