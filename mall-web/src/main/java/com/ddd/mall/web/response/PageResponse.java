package com.ddd.mall.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 通用分页响应
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer page;
    private Integer size;
}
