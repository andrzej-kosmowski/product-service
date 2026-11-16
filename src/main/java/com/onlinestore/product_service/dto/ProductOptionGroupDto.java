package com.onlinestore.product_service.dto;

import com.onlinestore.product_service.model.SelectionType;

public record ProductOptionGroupDto(
        Long id,
        String name,
        String code,
        SelectionType selectionType,
        Boolean required,
        Integer minSelections,
        Integer maxSelections
) {

}
