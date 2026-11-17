package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class ProductAlreadyExistsException extends ProductServiceException {
    public ProductAlreadyExistsException(String sku) {
        super("Product with SKU " + sku + " already exists", HttpStatus.CONFLICT);
    }
}
