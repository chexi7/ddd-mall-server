package com.ddd.mall.domain.product.query;

/**
 * 商品查询端口（CQRS 读侧）
 * 应用层 QueryService 通过此接口查询数据，基础设施层实现。
 */
public interface ProductQueryPort {

    /**
     * 管理端 / C端商品分页查询，page 从 1 开始
     */
    ProductPageResult findPage(int page, int size, String status, String category, String keyword);

    /**
     * 在售商品列表（按创建时间倒序）
     */
    java.util.List<com.ddd.mall.domain.product.Product> findOnSaleProductsOrdered();

    /**
     * 所有商品分类名称
     */
    java.util.List<String> findAllCategories();

    /**
     * 商品总数
     */
    long countTotal();
}