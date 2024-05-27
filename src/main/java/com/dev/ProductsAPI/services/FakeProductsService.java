package com.dev.ProductsAPI.services;

import com.dev.ProductsAPI.dtos.ProductApiDTO;
import com.dev.ProductsAPI.exceptions.ApiOutOfServiceException;
import com.dev.ProductsAPI.exceptions.ProductNotFoundException;
import com.dev.ProductsAPI.models.ProductModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class FakeProductsService {
    private String baseUrl = "https://fakestoreapi.com";

    @Autowired
    private RestTemplate restTemplate;

    public List<ProductModel> getProductsList() throws ApiOutOfServiceException {
        try {
            ProductApiDTO[] productApiDTOArray = restTemplate.getForObject(baseUrl+"/products", ProductApiDTO[].class);
            Optional<ProductApiDTO[]> lista = Optional.ofNullable(productApiDTOArray);
            return lista.map(productApiDTOS -> Arrays.stream(productApiDTOS)
                            .map(this::convertToProductModel).toList())
                    .orElseThrow(() -> new ProductNotFoundException("No products found"));
        } catch (HttpStatusCodeException e) {
            throw new ApiOutOfServiceException("The API fake store is out of service", e);
        }
    }

    public ProductModel convertToProductModel(ProductApiDTO product) {
        ProductModel newProductModel = new ProductModel();
        newProductModel.setName(product.getName());
        newProductModel.setValue(product.getValue());
        return newProductModel;
    }

}