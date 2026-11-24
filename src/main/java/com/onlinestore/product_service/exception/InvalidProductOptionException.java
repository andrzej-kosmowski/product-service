package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidProductOptionException extends ProductServiceException {
    public InvalidProductOptionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
