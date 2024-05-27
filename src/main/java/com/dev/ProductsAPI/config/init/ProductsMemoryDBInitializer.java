package com.dev.ProductsAPI.config.init;

import com.dev.ProductsAPI.models.ProductModel;
import com.dev.ProductsAPI.repository.primary.ProductRepository;
import com.dev.ProductsAPI.repository.secondary.ProductsMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductsMemoryDBInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductsMemoryRepository productsMemoryRepository;

    @Autowired
    public ProductsMemoryDBInitializer(ProductRepository productRepository, ProductsMemoryRepository productsMemoryRepository) {
        this.productRepository = productRepository;
        this.productsMemoryRepository = productsMemoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<ProductModel> productModelList = productRepository.findAll();
        productModelList.forEach(productsMemoryRepository::save);
    }
}