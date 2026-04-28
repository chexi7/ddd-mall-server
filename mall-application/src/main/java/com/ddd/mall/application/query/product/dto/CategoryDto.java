package com.ddd.mall.application.query.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类读模型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 子分类列表
     */
    private List<CategoryDto> children;
}