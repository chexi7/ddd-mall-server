package com.ddd.mall.domain.shared;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * <p>
 * 注意：version 字段不暴露 setter。version 是乐观锁标识，仅供仓储在 save() 后
 * 通过反射（DomainObjectReconstructor.setIdAndVersion）回写，业务代码不应直接修改。
 */
@Getter
public abstract class AggregateRoot extends Entity {

    /**
     * 版本号（乐观锁）
     */
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