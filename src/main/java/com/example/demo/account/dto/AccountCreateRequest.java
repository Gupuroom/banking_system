package com.example.demo.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Schema(description = "계좌 생성 요청 DTO")
@Builder
public record AccountCreateRequest(
        @Schema(description = "계좌 번호", example = "1234567890")
        String accountNumber,

        @Schema(description = "초기 잔액", example = "10000")
        BigDecimal initialBalance
) {
}