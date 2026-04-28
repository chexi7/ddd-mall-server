package com.ddd.mall.domain.shared;

import lombok.Getter;

import java.util.Objects;

/**
 * 实体基类
 * 实体通过唯一标识（ID）来区分，而非属性值
 * <p>
 * 注意：id 字段不暴露 setter。ID 是实体的身份标识，一旦创建不应被外部修改。
 * 仓储重建通过反射（DomainObjectReconstructor）设置 id，而非 public setter。
 */
@Getter
public abstract class Entity {

    /**
     * 实体唯一标识
     */
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}