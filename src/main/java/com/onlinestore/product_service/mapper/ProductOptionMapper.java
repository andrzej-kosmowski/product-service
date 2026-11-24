package com.onlinestore.product_service.mapper;

import com.onlinestore.product_service.command.CreateProductOptionCommand;
import com.onlinestore.product_service.dto.OptionProductDto;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOption;
import com.onlinestore.product_service.model.ProductOptionGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductOptionMapper {
    ProductOptionDto toDto(ProductOption option);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "name", source = "command.name")
    @Mapping(target = "code", source = "command.code")
    @Mapping(target = "value", source = "command.value")
    @Mapping(target = "group", source = "group")
    @Mapping(target = "optionProduct", source = "optionProduct")
    ProductOption from(CreateProductOptionCommand command, ProductOptionGroup group, Product optionProduct);
    OptionProductDto toOptionProductDto(Product product);
}
