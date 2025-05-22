package com.example.banking.domain.transaction.service;

import com.example.banking.core.error.BusinessException;
import com.example.banking.domain.account.entity.Account;
import com.example.banking.domain.account.error.AccountErrorCode;
import com.example.banking.domain.account.repository.AccountRepository;
import com.example.banking.domain.account.validation.AccountValidator;
import com.example.banking.domain.transaction.dto.TransactionHistoryResponse;
import com.example.banking.domain.transaction.dto.TransactionResponse;
import com.example.banking.domain.transaction.entity.Transaction;
import com.example.banking.domain.transaction.repository.TransactionRepository;
import com.example.banking.domain.transaction.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    private final TransactionRepository transactionRepository;
    private final TransactionValidator transactionValidator;

    @Transactional
    public TransactionResponse deposit(String accountNumber, BigDecimal amount) {
        // 검증
        transactionValidator.validateDeposit(accountNumber, amount);

        // 거래 처리 (비관적 락 적용)
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
            .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
        account.deposit(amount);
        
        // 거래 내역 저장
        Transaction transaction = Transaction.createDeposit(account, amount);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional
    public TransactionResponse withdraw(String accountNumber, BigDecimal amount) {
        // 검증
        transactionValidator.validateWithdrawal(accountNumber, amount);

        // 거래 처리 (비관적 락 적용)
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
            .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
        account.withdraw(amount);
        
        // 거래 내역 저장
        Transaction transaction = Transaction.createWithdrawal(account, amount);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional
    public TransactionResponse transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        // 1. 이체 검증
        transactionValidator.validateTransfer(fromAccountNumber, toAccountNumber, amount);

        // 2. 계좌 조회 (비관적 락 적용)
        Account fromAccount = accountRepository.findByAccountNumberWithLock(fromAccountNumber)
            .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
        Account toAccount = accountRepository.findByAccountNumberWithLock(toAccountNumber)
            .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        // 3. 수수료 계산
        BigDecimal fee = fromAccount.calculateTransferFee(amount);
        BigDecimal totalAmount = amount.add(fee);

        // 4. 출금 계좌 잔액 업데이트
        fromAccount.withdraw(totalAmount);
        BigDecimal fromAccountBalanceAfterTransaction = fromAccount.getBalance();

        // 5. 입금 계좌 잔액 업데이트
        toAccount.deposit(amount);
        BigDecimal toAccountBalanceAfterTransaction = toAccount.getBalance();

        // 6. 거래 내역 생성 및 저장
        Transaction fromTransaction = Transaction.createTransferOut(
            fromAccount,
            amount,
            fee,
            fromAccountBalanceAfterTransaction,
            toAccountNumber
        );
        Transaction savedFromTransaction = transactionRepository.save(fromTransaction);

        Transaction toTransaction = Transaction.createTransferIn(
            toAccount,
            amount,
            BigDecimal.ZERO,
            toAccountBalanceAfterTransaction,
            fromAccountNumber
        );
        transactionRepository.save(toTransaction);

        // 출금 계좌의 거래 내역을 반환
        return TransactionResponse.from(savedFromTransaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> getTransactionHistory(String accountNumber, Pageable pageable) {
        // 계좌 존재 여부 검증
        accountValidator.validateAccountExists(accountNumber);

        Page<Transaction> transactions = transactionRepository.findByAccountNumberOrderByIdDesc(accountNumber, pageable);
        return transactions.map(TransactionHistoryResponse::from);
    }
}