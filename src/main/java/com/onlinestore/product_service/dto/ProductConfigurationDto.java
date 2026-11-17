package com.onlinestore.product_service.dto;

import java.util.List;

public record ProductConfigurationDto(
        Long productId,
        List<ProductOptionGroupDto> groups
) {
}
