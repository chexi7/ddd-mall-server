package com.ddd.mall.domain.shared;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额值对象（不可变）
 */
@Getter
@EqualsAndHashCode
public class Money {

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("金额不能为空或负数");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) { return new Money(amount); }
    public static Money of(double amount) { return new Money(BigDecimal.valueOf(amount)); }
    public static Money zero() { return new Money(BigDecimal.ZERO); }

    public Money add(Money other) { return new Money(this.amount.add(other.amount)); }

    public Money subtract(Money other) {
        if (this.amount.compareTo(other.amount) < 0) throw new DomainException("金额不足以扣减");
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    @Override
    public String toString() { return amount.toPlainString(); }
}
