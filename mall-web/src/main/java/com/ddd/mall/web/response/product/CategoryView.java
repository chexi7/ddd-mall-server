package com.ddd.mall.web.response.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 分类视图响应对象
 */
public class CategoryView {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级分类ID，顶级分类可为空
     */
    private Long parentId;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 子分类列表
     */
    private List<CategoryView> children;
}
