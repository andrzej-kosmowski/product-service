package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class ProductOptionGroupNotFoundException extends ProductServiceException {
    public ProductOptionGroupNotFoundException(Long id) {
        super("Product option group with id " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
