package com.example.demo.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountCreateRequest(
        @NotBlank(message = "계좌번호는 필수입니다")
        @Pattern(regexp = "^[0-9]{10,20}$", message = "계좌번호는 10~20자리 숫자만 가능합니다")
        String accountNumber,

        BigDecimal initialBalance
) {
}