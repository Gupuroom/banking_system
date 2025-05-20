package com.example.demo.account.dto;

import com.example.demo.account.entity.Account;
import com.example.demo.account.type.AccountStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponse(
    Long id,
    String accountNumber,
    BigDecimal balance,
    AccountStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
} 