package com.ddd.mall.application.query.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 通用分页查询结果（应用层）
 */
@Getter
@RequiredArgsConstructor
public class PageResult<T> {
    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;
}
