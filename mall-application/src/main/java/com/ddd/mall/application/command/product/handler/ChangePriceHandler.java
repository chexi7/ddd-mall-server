package com.ddd.mall.application.command.product.handler;

import com.ddd.mall.application.command.product.cmd.ChangePriceCommand;
import com.ddd.mall.domain.product.Product;
import com.ddd.mall.domain.product.ProductRepository;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePriceHandler {

    private final ProductRepository productRepository;

    @Transactional
    public void handle(ChangePriceCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainException("商品不存在: " + command.getProductId()));
        product.changePrice(Money.of(command.getNewPrice()));
        productRepository.save(product);
    }
}