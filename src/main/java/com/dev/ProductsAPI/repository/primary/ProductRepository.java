package com.dev.ProductsAPI.repository.primary;

import com.dev.ProductsAPI.models.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductModel, UUID> {
}
