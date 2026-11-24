package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class ProductOptionAlreadyExistsException extends ProductServiceException {
    public ProductOptionAlreadyExistsException(Long groupId, String code) {
        super("Product option with code " + code + " already exists for group with id " + groupId,
                HttpStatus.CONFLICT);
    }
}
