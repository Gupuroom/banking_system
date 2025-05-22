package com.example.banking.api.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "계좌 생성 요청")
public record AccountCreateRequest(
    @Schema(description = "계좌번호", example = "1234567890")
    @NotBlank(message = "계좌번호는 필수입니다")
    String accountNumber,

    @Schema(description = "초기 잔액", example = "10000.00")
    @NotNull(message = "초기 잔액은 필수입니다")
    @DecimalMin(value = "0.0", message = "초기 잔액은 0 이상이어야 합니다")
    BigDecimal initialBalance
) {} 