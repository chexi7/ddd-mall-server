package com.ddd.mall.application.command.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class ChangePriceCommand {
    private final Long productId;
    private final BigDecimal newPrice;
}
