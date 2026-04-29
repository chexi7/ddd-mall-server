package com.ddd.mall.application.command.product;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductRepository;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品聚合命令处理器，处理商品相关命令。
 * <p>
 * 对应 DDD 铁律：一个命令处理器只操作一个聚合，聚合仅可以在命令处理器中进行操作。
 */
@Service
@RequiredArgsConstructor
public class ProductCommandHandler {

    private final ProductRepository productRepository;

    /**
     * 处理创建商品命令。
     */
    @Transactional
    public Long handle(CreateProductCommand command) {
        Product product = new Product(
                command.getName(), command.getDescription(),
                Money.of(command.getPrice()), command.getCategory());
        productRepository.save(product);
        return product.getId();
    }

    /**
     * 处理上架商品命令。
     */
    @Transactional
    public void handle(PublishProductCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainException("商品不存在: " + command.getProductId()));
        product.publish();
        productRepository.save(product);
    }

    /**
     * 处理修改商品价格命令。
     */
    @Transactional
    public void handle(ChangePriceCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainException("商品不存在: " + command.getProductId()));
        product.changePrice(Money.of(command.getNewPrice()));
        productRepository.save(product);
    }
}