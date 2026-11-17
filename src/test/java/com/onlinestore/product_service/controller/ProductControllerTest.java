package com.onlinestore.product_service.controller;

import com.onlinestore.product_service.command.CreateProductCommand;
import com.onlinestore.product_service.command.UpdateProductCommand;
import com.onlinestore.product_service.dto.ProductConfigurationDto;
import com.onlinestore.product_service.dto.ProductDto;
import com.onlinestore.product_service.exception.ProductAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.model.ProductType;
import com.onlinestore.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {
    ProductController productController;
    ProductService productService;

    @BeforeEach
    void setUp() {
        this.productService = mock(ProductService.class);
        this.productController = new ProductController(productService);
    }

    @Test
    void getActiveProducts_TypeNotSpecified_AllActiveProductsReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductDto> productPage = Page.empty(pageable);
        when(productService.getActiveProducts(pageable)).thenReturn(productPage);
        // when
        Page<ProductDto> result = productController.getActiveProducts(null, pageable);
        // then
        assertEquals(productPage, result);
        verify(productService).getActiveProducts(pageable);
        verify(productService, never()).getActiveProductsByType(any(), any());
    }

    @Test
    void getActiveProducts_TypeSpecified_ProductsOfTypeReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ProductType type = ProductType.COMPUTER;
        Page<ProductDto> productPage = Page.empty(pageable);
        when(productService.getActiveProductsByType(type, pageable))
                .thenReturn(productPage);
        // when
        Page<ProductDto> result = productController.getActiveProducts(type, pageable);
        // then
        assertEquals(productPage, result);
        verify(productService).getActiveProductsByType(type, pageable);
        verify(productService, never()).getActiveProducts(any());
    }

    @Test
    void getProductConfiguration_ProductExists_ConfigurationReturned() {
        // given
        Long productId = 1L;
        ProductConfigurationDto configuration = new ProductConfigurationDto(productId, List.of());
        when(productService.getProductConfiguration(productId)).thenReturn(configuration);
        // when
        ProductConfigurationDto result = productController.getProductConfiguration(productId);
        // then
        assertEquals(configuration, result);
        verify(productService).getProductConfiguration(productId);
    }

    @Test
    void getProductConfiguration_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productService.getProductConfiguration(productId))
                .thenThrow(new ProductNotFoundException(productId));
        // when & then
        assertThrows(
                ProductNotFoundException.class,
                () -> productController.getProductConfiguration(productId)
        );
        verify(productService).getProductConfiguration(productId);
    }

    @Test
    void getActiveProductById_ProductExists_ProductReturned() {
        // given
        Long productId = 1L;
        ProductDto expectedProduct = new ProductDto(
                productId,
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("6150.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.getActiveProductById(productId)).thenReturn(expectedProduct);
        // when
        ProductDto result = productController.getActiveProductById(productId);
        // then
        assertAll(
                () -> assertEquals(expectedProduct, result),
                () -> assertEquals(productId, result.id()),
                () -> assertEquals("SKU-001", result.sku())
        );
        verify(productService).getActiveProductById(productId);
    }

    @Test
    void getActiveProductById_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 999L;
        when(productService.getActiveProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));
        // when
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productController.getActiveProductById(productId));
        // then
        assertAll(
                () -> assertNotNull(exception),
                () -> verify(productService).getActiveProductById(productId)
        );
    }

    @Test
    void createProduct_ValidCommand_ProductCreated() {
        // given
        CreateProductCommand command = new CreateProductCommand(
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        ProductDto expectedProduct = new ProductDto(
                1L,
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("6150.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.createProduct(command)).thenReturn(expectedProduct);
        // when
        ProductDto result = productController.createProduct(command);
        // then
        assertAll(
                () -> assertEquals(expectedProduct, result),
                () -> assertEquals(1L, result.id()),
                () -> assertEquals("SKU-001", result.sku())
        );
        verify(productService).createProduct(command);
    }

    @Test
    void createProduct_DuplicateSku_ProductAlreadyExistsExceptionThrown() {
        // given
        CreateProductCommand command = new CreateProductCommand(
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.createProduct(command))
                .thenThrow(new ProductAlreadyExistsException(command.sku()));
        // when
        ProductAlreadyExistsException exception = assertThrows(
                ProductAlreadyExistsException.class, () -> productController.createProduct(command));
        // then
        assertNotNull(exception);
        verify(productService).createProduct(command);
    }

    @Test
    void updateProduct_ValidCommand_ProductUpdated() {
        // given
        Long productId = 1L;
        UpdateProductCommand command = new UpdateProductCommand(
                "SKU-001",
                "Updated Gaming PC",
                "Updated description",
                new BigDecimal("6000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        ProductDto expectedProduct = new ProductDto(
                productId,
                "SKU-001",
                "Updated Gaming PC",
                "Updated description",
                new BigDecimal("6000.00"),
                new BigDecimal("7380.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.updateProduct(productId, command)).thenReturn(expectedProduct);
        // when
        ProductDto result = productController.updateProduct(productId, command);
        // then
        assertAll(
                () -> assertEquals(expectedProduct, result),
                () -> assertEquals(productId, result.id()),
                () -> assertEquals("Updated Gaming PC", result.name()),
                () -> assertEquals(new BigDecimal("7380.00"), result.grossPrice())
        );
        verify(productService).updateProduct(productId, command);
    }

    @Test
    void updateProduct_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 999L;
        UpdateProductCommand command = new UpdateProductCommand(
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.updateProduct(productId, command))
                .thenThrow(new ProductNotFoundException(productId));
        // when
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productController.updateProduct(productId, command));
        // then
        assertNotNull(exception);
        verify(productService).updateProduct(productId, command);
    }

    @Test
    void updateProduct_DuplicateSku_ProductAlreadyExistsExceptionThrown() {
        // given
        Long productId = 1L;
        UpdateProductCommand command = new UpdateProductCommand(
                "SKU-002",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.updateProduct(productId, command))
                .thenThrow(new ProductAlreadyExistsException(command.sku()));
        // when
        ProductAlreadyExistsException exception = assertThrows(
                ProductAlreadyExistsException.class,
                () -> productController.updateProduct(productId, command)
        );
        // then
        assertNotNull(exception);
        verify(productService).updateProduct(productId, command);
    }

    @Test
    void activateProduct_ProductExists_ProductActivated() {
        // given
        Long productId = 1L;
        // when
        productController.activateProduct(productId);
        // then
        verify(productService).activateProduct(productId);
    }

    @Test
    void activateProduct_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 999L;
        doThrow(new ProductNotFoundException(productId))
                .when(productService).activateProduct(productId);
        // when & then
        assertThrows(
                ProductNotFoundException.class,
                () -> productController.activateProduct(productId)
        );
        verify(productService).activateProduct(productId);
    }

    @Test
    void deactivateProduct_ProductExists_ProductDeactivated() {
        // given
        Long productId = 1L;
        // when
        productController.deactivateProduct(productId);
        // then
        verify(productService).deactivateProduct(productId);
    }

    @Test
    void deactivateProduct_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 999L;
        doThrow(new ProductNotFoundException(productId))
                .when(productService).deactivateProduct(productId);
        // when & then
        assertThrows(
                ProductNotFoundException.class,
                () -> productController.deactivateProduct(productId)
        );
        verify(productService).deactivateProduct(productId);
    }
}