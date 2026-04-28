package com.ddd.mall.application.command.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class ChangePriceCommand {

    /**
     * 商品ID
     */
    private final Long productId;

    /**
     * 新价格
     */
    private final BigDecimal newPrice;
}