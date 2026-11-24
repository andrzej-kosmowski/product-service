package com.onlinestore.product_service.command;

import com.onlinestore.product_service.model.SelectionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductOptionGroupCommand(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Size(max = 50)
        String code,
        @NotNull
        SelectionType selectionType,
        @NotNull
        Boolean required,
        @NotNull
        @Min(0)
        Integer minSelections,
        @Min(1)
        Integer maxSelections
) {
}
