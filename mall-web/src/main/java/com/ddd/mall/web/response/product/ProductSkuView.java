package com.ddd.mall.web.response.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 商品SKU视图响应对象
 */
public class ProductSkuView {
    /**
     * SKU ID
     */
    private Long id;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU属性键值对
     */
    private Map<String, String> attributes;

    /**
     * 当前售价
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存数量
     */
    private Integer stock;
}
