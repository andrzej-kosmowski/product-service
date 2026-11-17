package com.onlinestore.product_service.dto;

import java.time.LocalDateTime;

public record ErrorMessageDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
}
