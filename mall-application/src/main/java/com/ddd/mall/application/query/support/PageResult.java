package com.ddd.mall.application.query.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页查询结果（应用层）。
 * <p>
 * 字段名与前端约定对齐：pageNum / pageSize / totalCount / data。
 * 使用 Builder 模式便于 QueryService 组装结果。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> data;

    /**
     * 总记录数
     */
    private long totalCount;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 页码
     */
    private int pageNum;

    /**
     * 每页条数
     */
    private int pageSize;
}