package com.dev.ProductsAPI.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductApiDTO {
    @JsonAlias("title")
    private String name;
    @JsonAlias("price")
    private BigDecimal value;
}

