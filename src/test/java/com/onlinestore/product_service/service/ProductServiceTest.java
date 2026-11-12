package com.onlinestore.product_service.service;

import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOption;
import com.onlinestore.product_service.model.ProductOptionGroup;
import com.onlinestore.product_service.model.ProductType;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductOptionRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    ProductService productService;
    ProductRepository productRepository;
    ProductOptionGroupRepository productOptionGroupRepository;
    ProductOptionRepository productOptionRepository;

    @BeforeEach
    void setUp() {
        this.productRepository = Mockito.mock(ProductRepository.class);
        this.productOptionGroupRepository = Mockito.mock(ProductOptionGroupRepository.class);
        this.productOptionRepository = Mockito.mock(ProductOptionRepository.class);
        this.productService = new ProductService(
                productRepository, productOptionGroupRepository, productOptionRepository);
    }

    @Test
    void getActiveProducts_ProductsExist_ProductsReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(
                new Product(),
                new Product()
        ));
        when(productRepository.findAllByActiveTrue(pageable))
                .thenReturn(expectedPage);
        // when
        Page<Product> result = productService.getActiveProducts(pageable);
        // then
        assertEquals(expectedPage, result);
        verify(productRepository).findAllByActiveTrue(pageable);
        verifyNoInteractions(productOptionGroupRepository, productOptionRepository);
    }

    @Test
    void getActiveProductsByType_TypeProvided_ProductsReturned() {
        // given
        ProductType type = ProductType.COMPUTER;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(
                new Product(),
                new Product()
        ));
        when(productRepository.findAllByTypeAndActiveTrue(type, pageable)).thenReturn(expectedPage);
        // when
        Page<Product> result = productService.getActiveProductsByType(type, pageable);
        // then
        assertEquals(expectedPage, result);
        verify(productRepository).findAllByTypeAndActiveTrue(type, pageable);
    }

    @Test
    void getActiveProductById_ProductExist_ProductReturned() {
        // given
        Long productId = 1L;
        Product product = new Product();
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        // when
        Product result = productService.getActiveProductById(productId);
        // then
        assertEquals(product, result);
        verify(productRepository).findByIdAndActiveTrue(productId);
    }

    @Test
    void getActiveProductById_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getActiveProductById(productId)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
    }

    @Test
    void getActiveOptionGroups_ProductExists_ActiveGroupsReturned() {
        // given
        Long productId = 1L;
        List<ProductOptionGroup> expectedGroups = List.of(
                new ProductOptionGroup(),
                new ProductOptionGroup()
        );
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(new Product()));
        when(productOptionGroupRepository.findAllByProductIdAndActiveTrue(productId)).thenReturn(expectedGroups);
        // when
        List<ProductOptionGroup> result = productService.getActiveOptionGroups(productId);
        // then
        assertEquals(expectedGroups, result);
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository).findAllByProductIdAndActiveTrue(productId);
    }

    @Test
    void getActiveOptionGroups_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getActiveOptionGroups(productId)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionGroupRepository);
    }

    @Test
    void getActiveOptions_GroupAndProductExist_ActiveOptionsReturned() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        ProductOptionGroup group = new ProductOptionGroup();
        group.setProduct(product);
        List<ProductOption> expectedOptions = List.of(
                new ProductOption(),
                new ProductOption()
        );
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId)).thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        when(productOptionRepository.findAllByGroupIdAndActiveTrue(groupId)).thenReturn(expectedOptions);
        // when
        List<ProductOption> result = productService.getActiveOptions(groupId);
        // then
        assertEquals(expectedOptions, result);
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionRepository).findAllByGroupIdAndActiveTrue(groupId);
    }

    @Test
    void getActiveOptions_GroupDoesNotExist_ProductOptionGroupNotFoundExceptionThrown() {
        // given
        Long groupId = 1L;
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductOptionGroupNotFoundException.class,
                () -> productService.getActiveOptions(groupId)
        );
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verifyNoInteractions(productRepository, productOptionRepository);
    }

    @Test
    void getActiveOptions_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        ProductOptionGroup group = new ProductOptionGroup();
        group.setProduct(product);
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId)).thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getActiveOptions(groupId)
        );
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionRepository);
    }
}