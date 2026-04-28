package com.ddd.mall.application.query.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 分页查询入参基类。
 * <p>
 * 分页字段（pageNum / pageSize）允许外部设置默认值，因此使用可变模式；
 * 而各 *Query 子类的业务筛选字段使用 final 不可变模式。
 * 继承本类后，子类只需声明自己的筛选字段即可。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {

    /**
     * 页码
     */
    private int pageNum = 1;

    /**
     * 每页条数
     */
    private int pageSize = 10;
}