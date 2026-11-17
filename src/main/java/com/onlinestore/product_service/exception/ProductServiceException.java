package com.onlinestore.product_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ProductServiceException extends RuntimeException {
    private final HttpStatus status;
    protected ProductServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
