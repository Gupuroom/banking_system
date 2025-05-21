package com.example.demo.transaction.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionRequest(
    BigDecimal amount
) {
} 