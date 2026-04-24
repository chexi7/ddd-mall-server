package com.ddd.mall.domain.shared;

import java.time.LocalDateTime;

/**
 * 领域事件标记接口
 * 所有领域事件都必须实现此接口
 */
public interface DomainEvent {

    /**
     * 事件发生时间
     */
    LocalDateTime occurredOn();
}
