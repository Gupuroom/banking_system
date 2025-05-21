package com.example.demo.transaction.validation;

import com.example.demo.account.entity.Account;
import com.example.demo.account.validation.AccountValidator;
import com.example.demo.common.error.BusinessException;
import com.example.demo.common.validation.CommonValidator;
import com.example.demo.transaction.error.TransactionErrorCode;
import com.example.demo.transaction.repository.TransactionRepository;
import com.example.demo.transaction.type.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionValidator {
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("10000000");
    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("1000000");

    private final CommonValidator commonValidator;
    private final AccountValidator accountValidator;
    private final TransactionRepository transactionRepository;

    public void validateDeposit(String accountNumber, BigDecimal amount) {
        // 1. 기본 금액 검증
        commonValidator.validateAmountFormat(amount);
        commonValidator.validatePositiveAmount(amount);
        validateMaxAmount(amount);

        // 2. 계좌 검증
        Account account = accountValidator.validateAccountExists(accountNumber);
        accountValidator.validateAccountStatus(account);
    }

    public void validateWithdrawal(String accountNumber, BigDecimal amount) {
        // 1. 기본 금액 검증
        commonValidator.validateAmountFormat(amount);
        commonValidator.validatePositiveAmount(amount);
        validateMaxAmount(amount);

        // 2. 계좌 검증
        Account account = accountValidator.validateAccountExists(accountNumber);
        accountValidator.validateAccountStatus(account);

        // 3. 잔액 검증
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(TransactionErrorCode.INSUFFICIENT_BALANCE);
        }

        // 4. 일일 출금 한도 검증
        validateDailyWithdrawalLimit(account, amount);
    }

    private void validateMaxAmount(BigDecimal amount) {
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BusinessException(TransactionErrorCode.AMOUNT_TOO_LARGE);
        }
    }

    private void validateDailyWithdrawalLimit(Account account, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        BigDecimal dailyAmount = transactionRepository.getDailyTransactionAmount(
            account, TransactionType.WITHDRAWAL, startOfDay, endOfDay);

        if (dailyAmount.add(amount).compareTo(DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new BusinessException(TransactionErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);
        }
    }
}