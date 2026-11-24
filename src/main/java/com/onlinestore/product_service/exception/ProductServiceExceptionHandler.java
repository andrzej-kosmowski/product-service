package com.onlinestore.product_service.exception;

import com.onlinestore.product_service.dto.ErrorMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ProductServiceExceptionHandler {
    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<ErrorMessageDto> handleProductServiceException(
            ProductServiceException ex
    ) {
        HttpStatus status = ex.getStatus();
        ErrorMessageDto error = new ErrorMessageDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );
        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageDto> handleUnexpectedException(
            Exception ex
    ) {
        log.error("Unexpected error occurred", ex);
        ErrorMessageDto error = new ErrorMessageDto(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred"
        );
        return ResponseEntity
                .internalServerError()
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageDto> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorMessageDto error = new ErrorMessageDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessageDto> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        String message = "Invalid value for parameter: " + ex.getName();

        ErrorMessageDto error = new ErrorMessageDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorMessageDto> handleMissingRequestParameter(
            MissingServletRequestParameterException ex
    ) {
        String message = "Missing required parameter: " + ex.getParameterName();

        ErrorMessageDto error = new ErrorMessageDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message
        );

        return ResponseEntity.badRequest().body(error);
    }
}
