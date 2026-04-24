package com.ddd.mall.domain.shared;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 实体基类
 * 实体通过唯一标识（ID）来区分，而非属性值
 */
@Getter
@Setter
public abstract class Entity {

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
