package com.onlinestore.product_service.mapper;

import com.onlinestore.product_service.command.CreateProductCommand;
import com.onlinestore.product_service.dto.ProductDto;
import com.onlinestore.product_service.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "optionGroups", ignore = true)
    @Mapping(target = "grossPrice", ignore = true)
    Product from(CreateProductCommand command);
    ProductDto toDto(Product product);
}
