package com.ddd.mall.domain.order;

/**
 * 订单状态（值对象 / 枚举）
 *
 * 状态机：
 * PENDING_PAYMENT → PAID → SHIPPED → COMPLETED
 *                        ↘ CANCELLED
 * PENDING_PAYMENT → CANCELLED
 */
public enum OrderStatus {

    /**
     * 待支付
     */
    PENDING_PAYMENT,

    /**
     * 已支付
     */
    PAID,

    /**
     * 已发货
     */
    SHIPPED,

    /**
     * 已完成
     */
    COMPLETED,

    /**
     * 已取消
     */
    CANCELLED
}