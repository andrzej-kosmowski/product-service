package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class ProductOptionGroupAlreadyExistsException extends ProductServiceException {
    public ProductOptionGroupAlreadyExistsException(Long productId, String code) {
        super("Product option group with code " + code + " already exists for product with id " + productId,
                HttpStatus.CONFLICT);
    }
}
