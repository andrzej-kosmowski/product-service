package com.onlinestore.product_service.exception;

public class ProductOptionGroupNotFoundException extends RuntimeException {
    public ProductOptionGroupNotFoundException(Long id) {
        super("Product option group with id " + id + " not found");
    }
}
