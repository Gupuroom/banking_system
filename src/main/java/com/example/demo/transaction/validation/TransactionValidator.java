package com.example.demo.transaction.validation;

import com.example.demo.account.entity.Account;
import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.validation.AccountValidator;
import com.example.demo.common.error.BusinessException;
import com.example.demo.common.error.CommonErrorCode;
import com.example.demo.common.validation.CommonValidator;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.error.TransactionErrorCode;
import com.example.demo.transaction.repository.TransactionRepository;
import com.example.demo.transaction.type.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class TransactionValidator {
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
        // 최대 거래 금액은 VIP 계좌의 이체 한도로 설정
        if (amount.compareTo(new BigDecimal("20000000")) > 0) {
            throw new BusinessException(TransactionErrorCode.AMOUNT_TOO_LARGE);
        }
    }

    private void validateDailyWithdrawalLimit(Account account, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        BigDecimal dailyAmount = transactionRepository.getDailyTransactionAmount(
            account, TransactionType.WITHDRAWAL, startOfDay, endOfDay);

        if (dailyAmount == null) {
            dailyAmount = BigDecimal.ZERO;
        }

        if (dailyAmount.add(amount).compareTo(account.getDailyWithdrawalLimit()) > 0) {
            throw new BusinessException(TransactionErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);
        }
    }

    public void validateTransfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        // 1. 기본 금액 검증
        commonValidator.validateAmountFormat(amount);
        commonValidator.validatePositiveAmount(amount);
        validateMaxAmount(amount);

        // 2. 자기 계좌 이체 방지 (계좌 존재 여부 검증 전에 수행)
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new BusinessException(TransactionErrorCode.SAME_ACCOUNT_TRANSFER);
        }

        // 3. 계좌 존재 여부 검증
        Account fromAccount = accountValidator.validateAccountExists(fromAccountNumber);
        Account toAccount = accountValidator.validateAccountExists(toAccountNumber);

        // 4. 계좌 상태 검증
        accountValidator.validateAccountStatus(fromAccount);
        accountValidator.validateAccountStatus(toAccount);

        // 5. 수수료 계산
        BigDecimal fee = fromAccount.calculateTransferFee(amount);
        BigDecimal totalAmount = amount.add(fee);

        // 6. 잔액 검증 (수수료 포함)
        if (fromAccount.getBalance().compareTo(totalAmount) < 0) {
            throw new BusinessException(TransactionErrorCode.INSUFFICIENT_BALANCE);
        }

        // 7. 일일 이체 한도 검증
        validateDailyTransferLimit(fromAccount, amount);
    }

    private void validateDailyTransferLimit(Account account, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        BigDecimal dailyAmount = transactionRepository.getDailyTransactionAmount(
            account, TransactionType.TRANSFER, startOfDay, endOfDay);
        
        if (dailyAmount == null) {
            dailyAmount = BigDecimal.ZERO;
        }

        if (dailyAmount.add(amount).compareTo(account.getDailyTransferLimit()) > 0) {
            throw new BusinessException(TransactionErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED);
        }
    }
}