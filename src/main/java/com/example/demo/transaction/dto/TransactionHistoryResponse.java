package com.example.demo.transaction.dto;

import com.example.demo.transaction.type.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionHistoryResponse(
    Long id,
    TransactionType type,
    BigDecimal amount,
    BigDecimal fee,
    BigDecimal balanceAfterTransaction,
    String relatedAccountNumber,
    LocalDateTime createdAt
) {
    public static TransactionHistoryResponse from(TransactionResponse transaction) {
        return TransactionHistoryResponse.builder()
            .id(transaction.id())
            .type(transaction.type())
            .amount(transaction.amount())
            .fee(transaction.fee())
            .balanceAfterTransaction(transaction.balanceAfterTransaction())
            .relatedAccountNumber(transaction.relatedAccountNumber())
            .createdAt(transaction.createdAt())
            .build();
    }
} 