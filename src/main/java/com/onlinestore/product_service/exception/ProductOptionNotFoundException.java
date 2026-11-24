package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class ProductOptionNotFoundException extends ProductServiceException {
    public ProductOptionNotFoundException(Long id) {
        super("Product option group with id " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
