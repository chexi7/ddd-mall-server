package com.ddd.mall.application.command.product.handler;

import com.ddd.mall.application.command.product.cmd.CreateProductCommand;
import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductRepository;
import com.ddd.mall.domain.shared.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProductHandler {

    private final ProductRepository productRepository;

    @Transactional
    public Long handle(CreateProductCommand command) {
        Product product = new Product(
                command.getName(), command.getDescription(),
                Money.of(command.getPrice()), command.getCategory());
        productRepository.save(product);
        return product.getId();
    }
}