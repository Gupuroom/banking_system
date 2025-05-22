package com.example.banking.domain.transaction.dto;

import com.example.banking.domain.transaction.entity.Transaction;
import com.example.banking.domain.transaction.type.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "거래 내역 응답 DTO")
@Builder
public record TransactionHistoryResponse(
    @Schema(description = "거래 ID", example = "1")
    Long id,

    @Schema(description = "거래 유형", example = "DEPOSIT")
    TransactionType type,

    @Schema(description = "거래 금액", example = "10000")
    BigDecimal amount,

    @Schema(description = "수수료", example = "100")
    BigDecimal fee,

    @Schema(description = "거래 후 잔액", example = "20000")
    BigDecimal balanceAfterTransaction,

    @Schema(description = "관련 계좌 번호 (이체인 경우)", example = "2345678910")
    String relatedAccountNumber,

    @Schema(description = "거래 일시", example = "2024-03-20T10:00:00")
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