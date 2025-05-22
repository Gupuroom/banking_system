package com.example.banking.core.error;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String code,
        String message
) {
    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }
} 