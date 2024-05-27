package com.dev.ProductsAPI.services;

import com.dev.ProductsAPI.dtos.ProductApiDTO;
import com.dev.ProductsAPI.exceptions.ApiOutOfServiceException;
import com.dev.ProductsAPI.exceptions.ProductNotFoundException;
import com.dev.ProductsAPI.models.ProductModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;



@ExtendWith(MockitoExtension.class)
class FakeProductsServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    FakeProductsService fakeProductsService;

    ProductApiDTO product1;
    ProductApiDTO product2;
    ProductApiDTO product3;

    @BeforeEach
    private void setUp() {
        product1 = new ProductApiDTO();
        product1.setName("prod1");
        product1.setValue(new BigDecimal("123.1"));
        product2 = new ProductApiDTO();
        product2.setName("prod2");
        product2.setValue(new BigDecimal("234.5"));
        product3 = new ProductApiDTO();
        product3.setName("prod3");
        product3.setValue(new BigDecimal("678.9"));
    }

    @Test
    public void should_Get_Product_List_From_FakeStoreAPI() {

        ProductApiDTO[] productApiDTOList = new ProductApiDTO[]{product1,product2,product3};
        when(restTemplate.getForObject(any(String.class), eq(ProductApiDTO[].class))).thenReturn(productApiDTOList);

        List<ProductModel> productModelList = fakeProductsService.getProductsList();
        assertEquals(productApiDTOList[0].getName(),productModelList.get(0).getName());
        assertEquals(productApiDTOList[1].getName(),productModelList.get(1).getName());
        assertEquals(productApiDTOList[2].getName(),productModelList.get(2).getName());
        assertEquals(productApiDTOList.length,productModelList.size());
    }

    @Test
    public void should_Throw_HttpStatusCodeException_When_API_Path_Is_Incorrect_Or_Offline() {
        when(restTemplate.getForObject(any(String.class), eq(ProductApiDTO[].class))).thenThrow(mock(HttpStatusCodeException.class));

        Throwable throwable = assertThrows(ApiOutOfServiceException.class, () -> fakeProductsService.getProductsList());

        assertEquals("The API fake store is out of service", throwable.getMessage());
    }

    @Test
    public void should_Throw_ProductNotFoundException_When_No_Products_Are_Found_Fetching_API() {
        when(restTemplate.getForObject(any(String.class), eq(ProductApiDTO[].class))).thenReturn(null);

        Throwable throwable = assertThrows(ProductNotFoundException.class, () -> fakeProductsService.getProductsList());

        assertEquals("No products found", throwable.getMessage());
    }

}