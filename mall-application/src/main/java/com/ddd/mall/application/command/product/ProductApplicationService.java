package com.ddd.mall.application.command.product;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductRepository;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品聚合应用服务，承接商品相关业务用例。
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;

    /**
     * 创建商品。
     *
     * @param command 创建商品命令
     * @return 新建商品 ID
     */
    @Transactional
    public Long createProduct(CreateProductCommand command) {
        Product product = new Product(
                command.getName(), command.getDescription(),
                Money.of(command.getPrice()), command.getCategory());
        productRepository.save(product);
        return product.getId();
    }

    /**
     * 上架商品。
     *
     * @param command 上架商品命令
     */
    @Transactional
    public void publishProduct(PublishProductCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainException("商品不存在: " + command.getProductId()));
        product.publish();
        productRepository.save(product);
    }

    /**
     * 修改商品价格。
     *
     * @param command 修改价格命令
     */
    @Transactional
    public void changePrice(ChangePriceCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainException("商品不存在: " + command.getProductId()));
        product.changePrice(Money.of(command.getNewPrice()));
        productRepository.save(product);
    }
}
