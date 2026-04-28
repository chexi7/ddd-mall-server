package com.ddd.mall.application.command.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PayOrderCommand {

    /**
     * 订单号
     */
    private final String orderNo;
}