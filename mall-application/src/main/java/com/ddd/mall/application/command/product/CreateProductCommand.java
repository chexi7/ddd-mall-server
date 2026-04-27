package com.ddd.mall.application.command.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class CreateProductCommand {
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String category;
}