package com.example.demo.account.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountCreateRequest(
        String accountNumber,

        BigDecimal initialBalance
) {
}