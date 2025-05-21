package com.example.demo.transaction.validation;

import com.example.demo.account.entity.Account;
import com.example.demo.account.validation.AccountValidator;
import com.example.demo.common.error.BusinessException;
import com.example.demo.common.validation.CommonValidator;
import com.example.demo.transaction.error.TransactionErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TransactionValidator {
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("10000000");

    private final CommonValidator commonValidator;
    private final AccountValidator accountValidator;

    public void validateDeposit(String accountNumber, BigDecimal amount) {
        // 1. 기본 금액 검증
        commonValidator.validateAmountFormat(amount);
        commonValidator.validatePositiveAmount(amount);
        validateMaxAmount(amount);

        // 2. 계좌 검증
        Account account = accountValidator.validateAccountExists(accountNumber);
        accountValidator.validateAccountStatus(account);
    }


    private void validateMaxAmount(BigDecimal amount) {
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BusinessException(TransactionErrorCode.AMOUNT_TOO_LARGE);
        }
    }
}