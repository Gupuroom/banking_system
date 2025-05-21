package com.example.demo.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "거래 요청 DTO")
public record TransactionRequest(
    @Schema(description = "거래 금액", example = "10000")
    @NotNull(message = "거래 금액은 필수입니다")
    BigDecimal amount
) {} 