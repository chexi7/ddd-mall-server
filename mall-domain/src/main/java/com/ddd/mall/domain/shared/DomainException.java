package com.ddd.mall.domain.shared;

/**
 * 领域异常
 * 用于表达业务规则校验失败等领域层异常
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
