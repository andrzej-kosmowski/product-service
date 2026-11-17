package com.onlinestore.product_service.dto;

public record ProductOptionDto(
        Long id,
        String name,
        String code,
        String value,
        OptionProductDto optionProduct
) {
}
