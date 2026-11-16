package com.onlinestore.product_service.mapper;

import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.model.ProductOptionGroup;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductOptionGroupMapper {
    ProductOptionGroupDto toDto(ProductOptionGroup group);
}
