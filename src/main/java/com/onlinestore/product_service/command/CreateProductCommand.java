package com.onlinestore.product_service.command;

import com.onlinestore.product_service.model.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductCommand(
        @NotBlank
        @Size(max = 50)
        String sku,
        @NotBlank
        @Size(max = 150)
        String name,
        String description,
        @NotNull
        @DecimalMin("0.00")
        BigDecimal netPrice,
        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal taxRate,
        @NotNull
        ProductType type
) {
}
