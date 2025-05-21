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
    public static TransactionHistoryResponse from(Transaction transaction) {
        return TransactionHistoryResponse.builder()
            .id(transaction.getId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .fee(transaction.getFee())
            .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
            .relatedAccountNumber(transaction.getRelatedAccountNumber())
            .createdAt(transaction.getCreatedAt())
            .build();
    }
} 