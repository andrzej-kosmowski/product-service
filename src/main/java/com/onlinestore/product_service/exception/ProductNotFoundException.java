package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ProductServiceException {
    public ProductNotFoundException(Long id) {
        super("Product with id " + id + " not found", HttpStatus.NOT_FOUND);
    }
}
