package com.example.demo.transaction.dto;

import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.type.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "거래 응답 DTO")
@Builder
public record TransactionResponse(
    @Schema(description = "거래 ID", example = "1")
    Long id,

    @Schema(description = "계좌 번호", example = "1234567891")
    String accountNumber,

    @Schema(description = "거래 유형", example = "DEPOSIT")
    TransactionType type,

    @Schema(description = "거래 금액", example = "10000")
    BigDecimal amount,

    @Schema(description = "거래 후 잔액", example = "20000")
    BigDecimal balanceAfterTransaction,

    @Schema(description = "수수료", example = "100")
    BigDecimal fee,

    @Schema(description = "관련 계좌 번호 (이체인 경우)", example = "2345678910")
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