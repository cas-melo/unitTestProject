package com.dev.ProductsAPI.controllers;


import com.dev.ProductsAPI.dtos.ProductRecordDto;
import com.dev.ProductsAPI.exceptions.ApiOutOfServiceException;
import com.dev.ProductsAPI.exceptions.NoContentException;
import com.dev.ProductsAPI.exceptions.ProductNotFoundException;
import com.dev.ProductsAPI.exceptions.ProductSaveException;
import com.dev.ProductsAPI.models.ProductModel;
import com.dev.ProductsAPI.repository.primary.ProductRepository;
import com.dev.ProductsAPI.repository.secondary.ProductsMemoryRepository;
import com.dev.ProductsAPI.services.ProductService;
import com.dev.ProductsAPI.utilsTest.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@AutoConfigureMockMvc
class ProductControllerTest {


    ProductModel product1;
    ProductModel product2;
    ProductModel product3;
    ProductModel product4;
    ProductModel product5;

    ProductRecordDto productDTO1;
    ProductRecordDto productDTO2;
    ProductRecordDto productDTO3;
    ProductRecordDto productDTO4;
    ProductRecordDto productDTO5;


    List<ProductModel> mockProductModelList;
    List<ProductRecordDto> mockProductDTOList;


    @BeforeEach
    private void setUp() {
        product1 = new ProductModel(UUID.fromString("cea90302-285e-4b26-bd75-ee3254246021"),"prod1",new BigDecimal("10.0"));
        product2 = new ProductModel(UUID.randomUUID(),"prod2",new BigDecimal("20.0"));
        product3 = new ProductModel(UUID.randomUUID(),"prod3",new BigDecimal("30.0"));
        product4 = new ProductModel(UUID.randomUUID(),"prod4",new BigDecimal("40.0"));
        product5 = new ProductModel(UUID.randomUUID(),"prod5",new BigDecimal("50.0"));

        productDTO1 = new ProductRecordDto("prod1", new BigDecimal("10.0"));
        productDTO2 = new ProductRecordDto("prod2", new BigDecimal("20.0"));
        productDTO3 = new ProductRecordDto("prod3",new BigDecimal("30.0"));
        productDTO4 = new ProductRecordDto("prod4",new BigDecimal("40.0"));
        productDTO5 = new ProductRecordDto("prod5",new BigDecimal("50.0"));

        mockProductModelList = List.of(product1,product2,product3,product4,product5);
        mockProductDTOList = List.of(productDTO1,productDTO2,productDTO3,productDTO4,productDTO5);
    }

    @SpringBootTest
    @Nested
    @DisplayName("method POST - fully service-mocked")
    class MockedPOST {

        @MockBean
        ProductService mockedProductService;

        @Autowired
        MockMvc mockMvc;

        @Test
        public void mocked_Post_To_saveDB_Should_Return_ResponseEntityOK_With_List_Of_Products() throws Exception {


            //retorno do metodo mockado para passar ao body da resposta
            when(mockedProductService.saveProductsIntoDB()).thenReturn(mockProductModelList);


            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/saveDB"));
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isOk();


            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                    });

        }
    }


    @SpringBootTest
    @Nested
    @DisplayName("method POST - not using mocked service and using Secondary DB")
    class NonMockedPOST {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ProductsMemoryRepository productsMemoryRepository;

        @MockBean
        ProductRepository mockedPrimaryProductRepository;

        @Autowired
        ProductService realProductService;


        @Test
        public void non_MOCKED_Post_To_saveDB_Should_Return_ResponseEntityOK_With_List_Of_Products() throws Exception {

            //quando uma invocação de um método do banco primario é feita
            //o argumento dessa invocação é salvo
            //e logo após é feita uma invocação do método equivalente ao método do banco primario, mas no banco secundário
            when(mockedPrimaryProductRepository.save(any(ProductModel.class))).then(invocationOnMock -> {
                ProductModel arg = invocationOnMock.getArgument(0);
                return productsMemoryRepository.save(arg);
            });


            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/saveDB"));
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isOk();


            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                    });
            //log do banco secundario (h2 em memoria), o primario está mockado
            System.out.println(productsMemoryRepository.findAll());

        }
    }

    @SpringBootTest
    @Nested
    @DisplayName("Normal mocked tests (fully mocked)")
    class NormalTests {

        @MockBean
        ProductService mockedProductService;

        @Autowired
        MockMvc mockMvc;

        ObjectMapper objectMapper = new ObjectMapper();


        @Test
        public void post_To_saveDB_Should_Return_ResponseEntity_ServiceUnavailable() throws Exception {
            when(mockedProductService.saveProductsIntoDB()).thenThrow(new ApiOutOfServiceException("The API fake store is out of service"));

            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/saveDB"));
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isServiceUnavailable();


            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void post_To_saveDB_Should_Return_ResponseEntity_NotFound() throws Exception {
            when(mockedProductService.saveProductsIntoDB()).thenThrow(new ProductNotFoundException("No products found") {
            });

            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/saveDB"));
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isNotFound();


            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void post_To_saveDB_Should_Return_ResponseEntity_InternalServerError() throws Exception {
            //Throwing a random unchecked exception(RuntimeException) just to get caught as Exception.class in the controller
            when(mockedProductService.saveProductsIntoDB()).thenThrow(new RuntimeException());

            Throwable throwable = assertThrows(Exception.class, () -> mockedProductService.saveProductsIntoDB());

            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/saveDB"));
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isInternalServerError();


            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals("Internal Server Error", resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void post_To_products_Should_Return_ResponseEntity_Created_And_List_Of_Saved_Products() throws Exception {
            when(mockedProductService.saveProducts(any())).thenReturn(mockProductModelList);

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/products")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(mockProductDTOList))
                    );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isCreated();

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                    });

        }

        @Test
        public void post_To_products_Should_Return_ResponseEntity_InternalServerError_ProductSaveException() throws Exception {
            when(mockedProductService.saveProducts(any())).thenThrow(new ProductSaveException("Failed to save product"));


            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/products")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(mockProductDTOList))
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isInternalServerError();

            Throwable throwable = assertThrows(ProductSaveException.class, () -> mockedProductService.saveProducts(any()));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals(Collections.emptyList().toString(), resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void post_To_products_Should_Return_ResponseEntity_InternalServerError_Exception() throws Exception {
            when(mockedProductService.saveProducts(any())).thenThrow(new RuntimeException());


            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/products")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(mockProductDTOList))
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isInternalServerError();

            Throwable throwable = assertThrows(Exception.class, () -> mockedProductService.saveProducts(any()));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals("Internal Server Error", resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void get_To_products_Should_Return_ResponseEntity_OK_With_PagedModel() throws Exception {
            int pageOfPagedModelList = 0;
            int sizeOfPagedModelList = 2;
            int totalElementsofList = mockProductModelList.size();

            List<EntityModel<ProductModel>> entityModelList = mockProductModelList.stream()
                    .map(product -> EntityModel.of(product, WebMvcLinkBuilder.linkTo(ProductController.class).slash(product.getIdProduct()).withSelfRel()))
                    .collect(Collectors.toList());

            PagedModel<EntityModel<ProductModel>> pagedModel = PagedModel.of(
                    entityModelList.subList(pageOfPagedModelList * sizeOfPagedModelList, Math.min((pageOfPagedModelList + 1) * sizeOfPagedModelList, entityModelList.size())),
                    new PagedModel.PageMetadata(sizeOfPagedModelList,pageOfPagedModelList,totalElementsofList));

            when(mockedProductService.getAllProducts(any())).thenReturn(pagedModel);


            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.get("/products?page="+pageOfPagedModelList+"&size="+sizeOfPagedModelList)
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isOk();


            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
//                        assertEquals(objectMapper.writeValueAsString(pagedModel), resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void get_To_products_Should_Return_ResponseEntity_NoContent() throws Exception {
            when(mockedProductService.getAllProducts(any())).thenThrow(new NoContentException("There are no products to list"));


            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.get("/products")
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isNoContent();

            Throwable throwable = assertThrows(NoContentException.class, () -> mockedProductService.getAllProducts(any()));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals(throwable.getMessage(), resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void get_To_products_Should_Return_ResponseEntity_Exception() throws Exception {
            when(mockedProductService.getAllProducts(any())).thenThrow(new RuntimeException());


            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.get("/products")
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isInternalServerError();

            Throwable throwable = assertThrows(Exception.class, () -> mockedProductService.getAllProducts(any()));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals("Internal Server Error", resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void get_To_products_ID_Should_Return_ResponseEntity_OK_With_A_Single_Product() throws Exception {
            when(mockedProductService.getOneProduct(product1.getIdProduct()))
                    .thenReturn(product1.add(linkTo(methodOn(ProductController.class).getAllProducts(Pageable.unpaged())).withRel("Products List:")));


            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.get("/products/"+ product1.getIdProduct())
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isOk();

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test // copiar nome base desse teste para os outros q lançam erro
        public void get_To_products_ID_Should_Return_ResponseEntity_NotFound_When_ProductNotFoundException() throws Exception {

            UUID randomUUID = UUID.randomUUID();
            when(mockedProductService.getOneProduct(randomUUID)).thenThrow(new ProductNotFoundException("This product was not found. Try again."));

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.get("/products/"+randomUUID)
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isNotFound();

            Throwable throwable = assertThrows(ProductNotFoundException.class, () -> mockedProductService.getOneProduct(randomUUID));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals(throwable.getMessage(), resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void get_To_products_ID_Should_Return_ResponseEntity_InternalServerError_When_Exception() throws Exception {

            UUID randomUUID = UUID.randomUUID();
            when(mockedProductService.getOneProduct(randomUUID)).thenThrow(new RuntimeException());

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.get("/products/"+randomUUID)
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isInternalServerError();

            Throwable throwable = assertThrows(Exception.class, () -> mockedProductService.getOneProduct(randomUUID));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals("Internal Server Error", resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void put_To_products_ID_Should_Return_ResponseEntity_OK_With_Changed_Product() throws Exception {
            ProductModel productUpdated = new ProductModel(product1.getIdProduct(),productDTO1.name(),productDTO1.value());


            when(mockedProductService.updateProduct(product1.getIdProduct(),productDTO1))
                    .thenReturn(productUpdated);
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.put("/products/"+product1.getIdProduct())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(productDTO1))
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isOk();


            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        String expected = TestUtils.readFileAsString("src/test/resources/arquivoJsonParaTeste.json");
                        assertEquals(expected, resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void put_To_products_ID_Should_Return_ResponseEntity_NotFound_When_ProductNotFoundException() throws Exception {
            when(mockedProductService.updateProduct(any(), any())).thenThrow(new ProductNotFoundException("This product was not found. Try again."));

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.put("/products/"+UUID.randomUUID())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(productDTO1))
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isNotFound();

            Throwable throwable = assertThrows(ProductNotFoundException.class, () -> mockedProductService.updateProduct(any(),any()));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals(throwable.getMessage(), resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void put_To_products_ID_Should_Return_ResponseEntity_InternalServerError_When_Exception() throws Exception {
            when(mockedProductService.updateProduct(any(), any())).thenThrow(new RuntimeException());

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.put("/products/"+UUID.randomUUID())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(productDTO1))
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isInternalServerError();

            Throwable throwable = assertThrows(Exception.class, () -> mockedProductService.updateProduct(any(),any()));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals("Internal Server Error", resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void delete_To_products_ID_Should_Return_ResponseEntity_OK() throws Exception {

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.delete("/products/"+product1.getIdProduct())
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isOk();

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals("Product deleted successfully.", resultHandler.getResponse().getContentAsString());
                    });

            verify(mockedProductService).deleteProduct(product1.getIdProduct());
        }

        @Test
        public void delete_To_products_ID_Should_Return_ResponseEntity_NotFound_When_ProductNotFoundException() throws Exception {
            UUID randomID = UUID.randomUUID();
            doThrow(new ProductNotFoundException("Product not found")).when(mockedProductService).deleteProduct(randomID);
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.delete("/products/"+randomID)
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isNotFound();

            Throwable throwable = assertThrows(ProductNotFoundException.class, () -> mockedProductService.deleteProduct(randomID));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals(throwable.getMessage(), resultHandler.getResponse().getContentAsString());
                    });
        }

        @Test
        public void delete_To_products_ID_Should_Return_ResponseEntity_InternalServerError_When_Exception() throws Exception {
            UUID randomID = UUID.randomUUID();
            doThrow(new RuntimeException()).when(mockedProductService).deleteProduct(randomID);

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.delete("/products/"+randomID)
            );
            ResultMatcher resultMatcher = MockMvcResultMatchers.status().isInternalServerError();

            Throwable throwable = assertThrows(Exception.class, () -> mockedProductService.deleteProduct(randomID));

            result.andExpect(resultMatcher)
                    .andDo(resultHandler -> {
                        System.out.println("BODY  -> " + resultHandler.getResponse().getContentAsString());
                        assertEquals("Internal Server Error", resultHandler.getResponse().getContentAsString());
                    });
        }
    }


}