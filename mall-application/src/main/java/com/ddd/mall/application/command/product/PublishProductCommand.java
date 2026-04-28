package com.ddd.mall.application.command.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PublishProductCommand {

    /**
     * 商品ID
     */
    private final Long productId;
}