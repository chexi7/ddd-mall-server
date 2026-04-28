package com.ddd.mall.domain.member.query;

/**
 * 会员查询端口（CQRS 读侧）
 * 应用层 QueryService 通过此接口查询数据，基础设施层实现。
 */
public interface MemberQueryPort {

    /** 会员总数 */
    long countTotal();
}