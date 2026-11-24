package com.onlinestore.product_service.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductOptionCommand(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Size(max = 50)
        String code,
        @Size(max = 150)
        String value,
        Long optionProductId
) {}