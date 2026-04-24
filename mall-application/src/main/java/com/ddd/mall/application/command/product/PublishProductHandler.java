package com.ddd.mall.application.command.product;

import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublishProductHandler {

    private final ProductRepository productRepository;

    @Transactional
    public void handle(PublishProductCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainException("商品不存在: " + command.getProductId()));
        product.publish();
        productRepository.save(product);
    }
}
