package com.onlinestore.product_service.mapper;

import com.onlinestore.product_service.command.CreateProductOptionGroupCommand;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.model.ProductOptionGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductOptionGroupMapper {
    ProductOptionGroupDto toDto(ProductOptionGroup group);

    @Mapping(target = "options", source = "options")
    ProductOptionGroupDto toDtoWithOptions(
            ProductOptionGroup group,
            List<ProductOptionDto> options
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "options", ignore = true)
    ProductOptionGroup from(CreateProductOptionGroupCommand command);
}
