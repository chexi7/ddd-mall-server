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
 * 商品视图响应对象
 */
public class ProductView {
    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品主图URL
     */
    private String mainImage;

    /**
     * 商品图片URL列表
     */
    private List<String> images;

    /**
     * 商品状态
     */
    private String status;

    /**
     * 商品SKU列表
     */
    private List<ProductSkuView> skus;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 创建时间
     */
    private String createdAt;
}
