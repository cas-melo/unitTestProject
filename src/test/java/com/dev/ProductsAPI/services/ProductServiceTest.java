package com.dev.ProductsAPI.services;

import com.dev.ProductsAPI.dtos.ProductRecordDto;
import com.dev.ProductsAPI.exceptions.NoContentException;
import com.dev.ProductsAPI.exceptions.ProductNotFoundException;
import com.dev.ProductsAPI.exceptions.ProductSaveException;
import com.dev.ProductsAPI.models.ProductModel;
import com.dev.ProductsAPI.repository.primary.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    FakeProductsService fakeProductsService;

    @Mock
    PagedResourcesAssembler<ProductModel> assembler;

    @InjectMocks
    ProductService productService;

    ProductModel product1;
    ProductModel product2;
    ProductModel product3;
    ProductModel product4;
    ProductModel product5;

    ProductRecordDto productDTO1;
    ProductRecordDto productDTO2;

    @BeforeEach
    private void setUp() {
        product1 = new ProductModel(UUID.randomUUID(),"prod1",new BigDecimal("10.0"));
        product2 = new ProductModel(UUID.randomUUID(),"prod2",new BigDecimal("20.0"));
        product3 = new ProductModel(UUID.randomUUID(),"prod3",new BigDecimal("30.0"));
        product4 = new ProductModel(UUID.randomUUID(),"prod4",new BigDecimal("40.0"));
        product5 = new ProductModel(UUID.randomUUID(),"prod5",new BigDecimal("50.0"));

        productDTO1 = new ProductRecordDto("prod1", new BigDecimal("10.0"));
        productDTO2 = new ProductRecordDto("prod2", new BigDecimal("20.0"));
    }

    @Test
    public void should_Consume_FakeProductsService_And_Save_Products_Into_DB() {

        List<ProductModel> productModelList = List.of(product1,product2,product3,product4);

        when(fakeProductsService.getProductsList()).thenReturn(productModelList);

        List<ProductModel> listReturned = productService.saveProductsIntoDB();

        verify(fakeProductsService,times(1)).getProductsList();

        verify(productRepository, times(1)).save(product1);
        verify(productRepository, times(1)).save(product2);
        verify(productRepository, times(1)).save(product3);
        verify(productRepository, times(1)).save(product4);

        assertEquals(productModelList, listReturned);
    }

    @Test
    public void should_Save_List_Of_Products() {

        List<ProductRecordDto> productRecordDtoList = List.of(productDTO1,productDTO2);

        List<ProductModel> savedProducts = new ArrayList<>();

        when(productRepository.save(any(ProductModel.class))).then(invocationOnMock -> {
            ProductModel productModelArgument = invocationOnMock.getArgument(0);
            ProductModel productModelModified = new ProductModel();

            BeanUtils.copyProperties(productModelArgument, productModelModified);
            productModelModified.setIdProduct(UUID.randomUUID());
            savedProducts.add(productModelModified);

            return productModelModified;
        });

        List<ProductModel> listReturned = productService.saveProducts(productRecordDtoList);

        verify(productRepository,times(2)).save(any(ProductModel.class));
        assertEquals(savedProducts,listReturned);

    }

    @Test
    public void should_Throw_ProductSaveException() {
        ProductRecordDto product1 = new ProductRecordDto("prod1", new BigDecimal("10.0"));
        List<ProductRecordDto> productRecordDtoList = List.of(product1);

        when(productRepository.save(any())).thenThrow(new RuntimeException());

        Throwable throwable = assertThrows(ProductSaveException.class, () -> productService.saveProducts(productRecordDtoList));

        assertEquals("Failed to save product", throwable.getMessage());

    }

    @Test
    public void should_Get_All_Products() {
        Pageable pageable = PageRequest.of(1,3);
        List<ProductModel> productModelList = List.of(product1, product2, product3, product4, product5);
        System.out.println(pageable.getOffset());
        System.out.println(pageable.getPageSize());
        System.out.println(pageable.getPageNumber());
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), productModelList.size());
        Page<ProductModel> page = new PageImpl<>(
                productModelList.subList(start, end),
                pageable,
                productModelList.size());

        PagedModel<EntityModel<ProductModel>> pagedModelTest =
                PagedModel.of(
                        List.of(
                                EntityModel.of(productModelList.get(3)),
                                EntityModel.of(productModelList.get(4))
                        ),
                        new PagedModel.PageMetadata(pageable.getPageSize(),pageable.getPageNumber(),productModelList.size())
                );

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(assembler.toModel(eq(page), any(RepresentationModelAssembler.class))).thenReturn(pagedModelTest);

        PagedModel<EntityModel<ProductModel>> pagedModelResult = productService.getAllProducts(pageable);
        assertEquals(pagedModelTest,pagedModelResult);
    }

    @Test
    public void should_Throw_An_Exception_Where_There_Is_No_Content_To_Get() {
        Pageable pageable = PageRequest.of(0,20);
        when(productRepository.findAll(pageable)).thenReturn(Page.empty());

        Throwable throwable = assertThrows(NoContentException.class, () -> productService.getAllProducts(pageable));
        assertEquals("There are no products to list", throwable.getMessage());

        verify(productRepository).findAll(pageable);
    }

    @Test
    public void should_Get_A_Single_Product() {
        UUID id = UUID.randomUUID();
        product1.setIdProduct(id);
        Optional<ProductModel> productModelOptional = Optional.of(product1);
        List<ProductModel> productModelList = List.of(product1);

        when(productRepository.findById(id)).thenReturn(productModelOptional);



        ProductModel productModelReturned = productService.getOneProduct(id);

        assertEquals(productModelOptional.get(), productModelReturned);
    }

    @Test
    public void should_Throw_Exception_When_Searching_For_An_Inexistent_Product() {
        UUID id = UUID.randomUUID();
        Throwable throwable = assertThrows(ProductNotFoundException.class, () -> productService.getOneProduct(id));
        assertEquals("This product was not found. Try again.", throwable.getMessage());
    }

    @Test
    public void should_Update_A_Product_Successfully() {
        UUID id = UUID.randomUUID();
        product5.setIdProduct(id);


        when(productRepository.findById(id)).thenReturn(Optional.of(product5));
        when(productRepository.save(product5)).thenReturn(product5);
        ProductModel productModelResult = productService.updateProduct(id, productDTO1);

        assertEquals(product5, productModelResult);

        verify(productRepository).save(product5);
    }

    @Test
    public void should_Throw_A_ProductNotFoundException_When_Updating_An_Inexistent_Product() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Throwable throwable = assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(id, any(ProductRecordDto.class)));
        assertEquals("This product was not found. Try again.", throwable.getMessage());
    }

    @Test
    public void should_Delete_A_Product_With_Success() {
        UUID id = UUID.randomUUID();
        product1.setIdProduct(id);
        Optional<ProductModel> productModelOptional = Optional.of(product1);
        when(productRepository.findById(id)).thenReturn(productModelOptional);

        productService.deleteProduct(id);

        verify(productRepository).delete(productModelOptional.get());
    }

    @Test
    public void should_Throw_ProductNotFoundException_When_Trying_To_Delete_Inexistent_Product() {
        UUID id = UUID.randomUUID();

        Throwable throwable = assertThrows(ProductNotFoundException.class ,() -> productService.deleteProduct(id));
        assertEquals("This product was not found. Try again.", throwable.getMessage());
        verify(productRepository,never()).delete(any(ProductModel.class));
    }

}