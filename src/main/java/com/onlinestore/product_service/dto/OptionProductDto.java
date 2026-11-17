package com.onlinestore.product_service.dto;

import java.math.BigDecimal;

public record OptionProductDto(
        Long id,
        String sku,
        String name,
        BigDecimal grossPrice
) {
}
