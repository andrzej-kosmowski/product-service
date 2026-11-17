package com.onlinestore.product_service.controller;

import com.onlinestore.product_service.command.CreateProductCommand;
import com.onlinestore.product_service.command.UpdateProductCommand;
import com.onlinestore.product_service.dto.ProductDto;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get active products", description = "Returns a paginated list of active products")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public Page<ProductDto> getActiveProducts(Pageable pageable) {
        log.info("Fetching active products. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return productService.getActiveProducts(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns an active product with the specified ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductDto getActiveProductById(@PathVariable Long id) {
        log.info("Fetching product by ID: {}", id);
        return productService.getActiveProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "409", description = "Product with the given SKU already exists")
    })
    public ProductDto createProduct(@Valid @RequestBody CreateProductCommand command) {
        log.info("Creating new product with sku={}", command.sku());
        ProductDto product =  productService.createProduct(command);
        log.info("Product created successfully with id={}", product.id());
        return product;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing active product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Product with the given SKU already exists")
    })
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductCommand command) {
        log.info("Updating product with id={}", id);
        ProductDto product = productService.updateProduct(id, command);
        log.info("Product updated successfully with id={}", product.id());
        return product;
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product activated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void activateProduct(@PathVariable Long id) {
        log.info("Activating product with id={}", id);
        productService.activateProduct(id);
        log.info("Product activated successfully with id={}", id);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void deactivateProduct(@PathVariable Long id) {
        log.info("Deactivating product with id={}", id);
        productService.deactivateProduct(id);
        log.info("Product deactivated successfully with id={}", id);
    }
}

