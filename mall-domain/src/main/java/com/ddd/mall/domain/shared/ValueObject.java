package com.ddd.mall.domain.shared;

/**
 * 值对象基类
 * 值对象没有唯一标识，通过所有属性值来判断相等性。
 * 值对象是不可变的 — 创建后不可修改，需要改变时创建新实例。
 */
public abstract class ValueObject {

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
