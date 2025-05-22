package com.example.banking.domain.transaction.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    DEPOSIT("입금"),
    WITHDRAWAL("출금"),
    TRANSFER_OUT("이체출금"),
    TRANSFER_IN("이체입금");

    private final String description;
}