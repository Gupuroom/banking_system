package com.example.demo.transaction.dto;

import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.type.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
    Long id,
    String accountNumber,
    TransactionType type,
    BigDecimal amount,
    BigDecimal balanceAfterTransaction,
    BigDecimal fee,
    String relatedAccountNumber,
    LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountNumber(transaction.getAccount().getAccountNumber())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .fee(transaction.getFee())
                .relatedAccountNumber(transaction.getRelatedAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
} 