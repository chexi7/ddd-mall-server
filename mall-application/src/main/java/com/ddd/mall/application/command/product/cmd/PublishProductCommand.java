package com.ddd.mall.application.command.product.cmd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PublishProductCommand {
    private final Long productId;
}