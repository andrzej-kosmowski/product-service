package com.onlinestore.product_service.mapper;

import com.onlinestore.product_service.dto.OptionProductDto;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductOptionMapper {
    ProductOptionDto toDto(ProductOption option);
    OptionProductDto toOptionProductDto(Product product);
}
