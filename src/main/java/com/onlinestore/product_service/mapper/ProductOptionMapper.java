package com.onlinestore.product_service.mapper;

import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.model.ProductOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductOptionMapper {
    @Mapping(target = "optionProductId", source = "optionProduct.id")
    ProductOptionDto toDto(ProductOption option);
}
