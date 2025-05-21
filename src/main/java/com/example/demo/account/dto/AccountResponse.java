package com.example.demo.account.dto;

import com.example.demo.account.entity.Account;
import com.example.demo.account.type.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "계좌 응답 DTO")
@Builder
public record AccountResponse(
    @Schema(description = "계좌 ID", example = "1")
    Long id,

    @Schema(description = "계좌 번호", example = "1234567890")
    String accountNumber,

    @Schema(description = "계좌 잔액", example = "10000")
    BigDecimal balance,

    @Schema(description = "계좌 상태", example = "ACTIVE")
    AccountStatus status,

    @Schema(description = "생성 일시", example = "2024-03-20T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2024-03-20T10:00:00")
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