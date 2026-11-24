package com.onlinestore.product_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidProductOptionGroupException extends ProductServiceException {
    public InvalidProductOptionGroupException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
