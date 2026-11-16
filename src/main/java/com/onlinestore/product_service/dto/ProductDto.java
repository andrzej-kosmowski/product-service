package com.onlinestore.product_service.dto;

import com.onlinestore.product_service.model.ProductType;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal netPrice,
        BigDecimal grossPrice,
        BigDecimal taxRate,
        ProductType type
) {
}
