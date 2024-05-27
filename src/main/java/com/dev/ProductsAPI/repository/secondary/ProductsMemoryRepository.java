package com.dev.ProductsAPI.repository.secondary;

import com.dev.ProductsAPI.models.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductsMemoryRepository extends JpaRepository <ProductModel, UUID>{
}
