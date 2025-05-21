package com.example.demo.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionRequest(
    @NotNull(message = "금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "금액은 0보다 커야 합니다")
    BigDecimal amount
) {
} 