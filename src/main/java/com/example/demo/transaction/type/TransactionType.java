package com.example.demo.transaction.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    DEPOSIT("입금"),
    WITHDRAWAL("출금"),
    TRANSFER("이체");

    private final String description;
}