package com.dev.ProductsAPI.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
public class RepositoriesConfig {

    @EnableJpaRepositories(
            basePackages = "com.dev.ProductsAPI.repository.primary",
            entityManagerFactoryRef = "primaryEntityManagerFactory",
            transactionManagerRef = "primaryTransactionManager"
    )
    static class PrimaryJpaRepositoriesConfig { }

    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.dev.ProductsAPI.repository.secondary",
            entityManagerFactoryRef = "secondaryEntityManagerFactory",
            transactionManagerRef = "secondaryTransactionManager"
    )
    static class SecondaryJpaRepositoriesConfig { }
}
