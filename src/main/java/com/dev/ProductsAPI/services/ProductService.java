package com.dev.ProductsAPI.services;

import com.dev.ProductsAPI.controllers.ProductController;
import com.dev.ProductsAPI.dtos.ProductRecordDto;
import com.dev.ProductsAPI.exceptions.NoContentException;
import com.dev.ProductsAPI.exceptions.ProductNotFoundException;
import com.dev.ProductsAPI.exceptions.ProductSaveException;
import com.dev.ProductsAPI.models.ProductModel;
import com.dev.ProductsAPI.repository.primary.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    FakeProductsService fakeProductsService;

    @Autowired
    private PagedResourcesAssembler<ProductModel> assembler;

    public List<ProductModel> saveProductsIntoDB() {
        List<ProductModel> productList = fakeProductsService.getProductsList();
        productList.forEach(product -> productRepository.save(product));
        return productList;
    }

    public List<ProductModel> saveProducts(List<ProductRecordDto> productRecordDto) {
        List<ProductModel> savedProducts = new ArrayList<>();
        try {
            for(ProductRecordDto productDto : productRecordDto) {
                ProductModel productModel = new ProductModel();
                BeanUtils.copyProperties(productDto, productModel);
                savedProducts.add(productRepository.save(productModel));
            }
        } catch (RuntimeException e){
            throw new ProductSaveException("Failed to save product", e);
        }
        return savedProducts;
    }

    public PagedModel<EntityModel<ProductModel>> getAllProducts(Pageable pageable) {
        Page<ProductModel> productsPage = productRepository.findAll(pageable);
        if (productsPage.isEmpty()) {
            throw new NoContentException("There are no products to list");
        }

        PagedModel<EntityModel<ProductModel>> pagedModel = assembler.toModel(
                productsPage,
                product -> EntityModel.of(product,
                        linkTo(methodOn(ProductController.class).getOneProduct(product.getIdProduct())).withSelfRel())
        );
        return pagedModel;
    }

    public ProductModel getOneProduct(UUID id) {
        Optional<ProductModel> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) throw new ProductNotFoundException("This product was not found. Try again.");

        productOptional.get().add(linkTo(methodOn(ProductController.class).getAllProducts(Pageable.unpaged())).withRel("Products List:"));
        return productOptional.get();
    }

    public ProductModel updateProduct(UUID id, ProductRecordDto productRecordDto) {
        Optional<ProductModel> productOptional = productRepository.findById(id);
        if(productOptional.isEmpty()) {
            throw new ProductNotFoundException("This product was not found. Try again.");
        }
        ProductModel productModel = productOptional.get();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return productRepository.save(productModel);
    }

    public void deleteProduct(UUID id){
        Optional<ProductModel> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new ProductNotFoundException("This product was not found. Try again.");
        }
        productRepository.delete(productOptional.get());
    }
}
