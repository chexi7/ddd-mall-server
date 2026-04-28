package com.ddd.mall.domain.shared;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 */
@Getter
public abstract class AggregateRoot extends Entity {

    /**
     * 版本号
     */
    @Setter
    private Long version;

    /**
     * 领域事件列表
     */
    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}