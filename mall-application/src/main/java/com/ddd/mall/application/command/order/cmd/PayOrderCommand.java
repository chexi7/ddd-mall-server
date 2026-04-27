package com.ddd.mall.application.command.order.cmd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PayOrderCommand {
    private final String orderNo;
}