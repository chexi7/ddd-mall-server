package com.ddd.mall.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.ddd.mall")
@EntityScan(basePackages = "com.ddd.mall.infrastructure.persistence.dataobject")
@EnableJpaRepositories(basePackages = "com.ddd.mall.infrastructure.persistence")
public class MallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallApplication.class, args);
    }
}
