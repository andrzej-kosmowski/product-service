package com.onlinestore.product_service.dto;

import com.onlinestore.product_service.model.SelectionType;

import java.util.List;

public record ProductOptionGroupDto(
        Long id,
        String name,
        String code,
        SelectionType selectionType,
        Boolean required,
        Integer minSelections,
        Integer maxSelections,
        List<ProductOptionDto> options

) {

}
