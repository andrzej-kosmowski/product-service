package com.onlinestore.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.product_service.command.CreateProductCommand;
import com.onlinestore.product_service.command.UpdateProductCommand;
import com.onlinestore.product_service.dto.ProductConfigurationDto;
import com.onlinestore.product_service.dto.ProductDto;
import com.onlinestore.product_service.exception.ProductAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductServiceExceptionHandler;
import com.onlinestore.product_service.model.ProductType;
import com.onlinestore.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = ProductController.class,
        properties = "spring.liquibase.enabled=false"
)
@Import(ProductServiceExceptionHandler.class)
public class ProductControllerMvcTest {
    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    ProductService productService;

    @Test
    void getActiveProducts_ProductsExist_ProductsReturned() throws Exception {
        // given
        ProductDto product = new ProductDto(
                1L,
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("6150.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        Page<ProductDto> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productService.getActiveProducts(any(Pageable.class)))
                .thenReturn(productPage);
        // when & then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$.content[0].name").value("Gaming PC"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
        verify(productService).getActiveProducts(any(Pageable.class));
    }

    @Test
    void getActiveProducts_TypeSpecified_ProductsOfTypeReturned() throws Exception {
        // given
        ProductDto product = new ProductDto(
                1L,
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("6150.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        Page<ProductDto> productPage = new PageImpl<>(
                List.of(product),
                PageRequest.of(0, 10),
                1
        );
        when(productService.getActiveProductsByType(eq(ProductType.COMPUTER), any(Pageable.class)))
                .thenReturn(productPage);
        // when & then
        mockMvc.perform(get("/api/products")
                        .param("type", "COMPUTER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("COMPUTER"));
        verify(productService).getActiveProductsByType(
                eq(ProductType.COMPUTER),
                any(Pageable.class)
        );
        verify(productService, never()).getActiveProducts(any());
    }

    @Test
    void getActiveProducts_InvalidType_BadRequestReturned() throws Exception {
        // when & then
        mockMvc.perform(get("/api/products")
                        .param("type", "TABLET"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @Test
    void getProductConfiguration_ProductExists_ConfigurationReturned() throws Exception {
        // given
        Long productId = 1L;
        ProductConfigurationDto configuration = new ProductConfigurationDto(
                productId,
                List.of()
        );
        when(productService.getProductConfiguration(productId)).thenReturn(configuration);
        // when & then
        mockMvc.perform(get("/api/products/{id}/configuration", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups").isEmpty());
        verify(productService).getProductConfiguration(productId);
    }

    @Test
    void getProductConfiguration_ProductDoesNotExist_NotFoundReturned() throws Exception {
        // given
        Long productId = 1L;
        when(productService.getProductConfiguration(productId))
                .thenThrow(new ProductNotFoundException(productId));
        // when & then
        mockMvc.perform(get("/api/products/{id}/configuration", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        verify(productService).getProductConfiguration(productId);
    }

    @Test
    void getActiveProductById_ProductExists_ProductReturned() throws Exception {
        // given
        Long productId = 1L;
        ProductDto product = new ProductDto(
                productId,
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("6150.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.getActiveProductById(productId)).thenReturn(product);
        // when & then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Gaming PC"))
                .andExpect(jsonPath("$.grossPrice").value(6150.00));
        verify(productService).getActiveProductById(productId);
    }

    @Test
    void getActiveProductById_ProductDoesNotExist_NotFoundReturned() throws Exception {
        // given
        Long productId = 999L;
        when(productService.getActiveProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));
        // when & then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
        verify(productService).getActiveProductById(productId);
    }

    @Test
    void createProduct_InvalidCommand_BadRequestReturned() throws Exception {
        // given
        CreateProductCommand command = new CreateProductCommand(
                "",
                "",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        // when & then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_ValidCommand_ProductCreated() throws Exception {
        // given
        CreateProductCommand command = new CreateProductCommand(
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        ProductDto product = new ProductDto(
                1L,
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("6150.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.createProduct(command)).thenReturn(product);
        // when & then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Gaming PC"));
        verify(productService).createProduct(command);
    }

    @Test
    void createProduct_DuplicateSku_ConflictReturned() throws Exception {
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
                .thenThrow(new ProductAlreadyExistsException("SKU-001"));
        // when & then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
        verify(productService).createProduct(command);
    }

    @Test
    void updateProduct_ValidCommand_ProductUpdated() throws Exception {
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
        ProductDto product = new ProductDto(
                productId,
                "SKU-001",
                "Updated Gaming PC",
                "Updated description",
                new BigDecimal("6000.00"),
                new BigDecimal("7380.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.updateProduct(productId, command)).thenReturn(product);
        // when & then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Updated Gaming PC"))
                .andExpect(jsonPath("$.grossPrice").value(7380.00));
        verify(productService).updateProduct(productId, command);
    }

    @Test
    void updateProduct_InvalidCommand_BadRequestReturned() throws Exception {
        // given
        Long productId = 1L;
        UpdateProductCommand command = new UpdateProductCommand(
                "",
                "",
                "Updated description",
                new BigDecimal("6000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        // when & then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @Test
    void updateProduct_ProductDoesNotExist_NotFoundReturned() throws Exception {
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
        // when & then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
        verify(productService).updateProduct(productId, command);
    }

    @Test
    void updateProduct_DuplicateSku_ConflictReturned() throws Exception {
        // given
        Long productId = 1L;
        UpdateProductCommand command = new UpdateProductCommand(
                "SKU-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productService.updateProduct(productId, command))
                .thenThrow(new ProductAlreadyExistsException("SKU-001"));
        // when & then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
        verify(productService).updateProduct(productId, command);
    }

    @Test
    void activateProduct_ProductExists_OkReturned() throws Exception {
        // given
        Long productId = 1L;
        // when & then
        mockMvc.perform(patch("/api/products/{id}/activate", productId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        verify(productService).activateProduct(productId);
    }

    @Test
    void activateProduct_ProductDoesNotExist_NotFoundReturned() throws Exception {
        // given
        Long productId = 999L;
        doThrow(new ProductNotFoundException(productId))
                .when(productService).activateProduct(productId);
        // when & then
        mockMvc.perform(patch("/api/products/{id}/activate", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
        verify(productService).activateProduct(productId);
    }

    @Test
    void deactivateProduct_ProductExists_OkReturned() throws Exception {
        // given
        Long productId = 1L;
        // when & then
        mockMvc.perform(patch("/api/products/{id}/deactivate", productId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        verify(productService).deactivateProduct(productId);
    }

    @Test
    void deactivateProduct_ProductDoesNotExist_NotFoundReturned() throws Exception {
        // given
        Long productId = 999L;
        doThrow(new ProductNotFoundException(productId))
                .when(productService).deactivateProduct(productId);
        // when & then
        mockMvc.perform(patch("/api/products/{id}/deactivate", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
        verify(productService).deactivateProduct(productId);
    }
}
