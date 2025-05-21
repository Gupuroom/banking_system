package com.example.demo.transaction.service;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.account.validation.AccountValidator;
import com.example.demo.transaction.dto.TransactionHistoryResponse;
import com.example.demo.transaction.dto.TransactionRequest;
import com.example.demo.transaction.dto.TransactionResponse;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import com.example.demo.transaction.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    private final TransactionRepository transactionRepository;
    private final TransactionValidator transactionValidator;

    @Transactional
    public TransactionResponse deposit(String accountNumber, TransactionRequest request) {
        // 검증
        transactionValidator.validateDeposit(accountNumber, request.amount());

        // 거래 처리
        Account account = accountRepository.findByAccountNumber(accountNumber).get();
        account.deposit(request.amount());
        Transaction transaction = Transaction.createDeposit(account, request.amount());
        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional
    public TransactionResponse withdraw(String accountNumber, TransactionRequest request) {
        // 검증
        transactionValidator.validateWithdrawal(accountNumber, request.amount());

        // 거래 처리
        Account account = accountRepository.findByAccountNumber(accountNumber).get();
        account.withdraw(request.amount());
        Transaction transaction = Transaction.createWithdrawal(account, request.amount());

        // 로그 추가
        System.out.println("Transaction type: " + transaction.getType());
        System.out.println("Transaction type name: " + transaction.getType().name());

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional
    public TransactionResponse transfer(String fromAccountNumber, String toAccountNumber, TransactionRequest request) {
        // 1. 이체 검증
        transactionValidator.validateTransfer(fromAccountNumber, toAccountNumber, request.amount());

        // 2. 계좌 조회
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber).get();
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber).get();

        // 3. 수수료 계산
        BigDecimal fee = fromAccount.calculateTransferFee(request.amount());
        BigDecimal totalAmount = request.amount().add(fee);

        // 4. 출금 계좌 잔액 업데이트
        fromAccount.withdraw(totalAmount);
        BigDecimal fromAccountBalanceAfterTransaction = fromAccount.getBalance();

        // 5. 입금 계좌 잔액 업데이트
        toAccount.deposit(request.amount());
        BigDecimal toAccountBalanceAfterTransaction = toAccount.getBalance();

        // 6. 거래 내역 생성 및 저장
        Transaction fromTransaction = Transaction.createTransferOut(
            fromAccount,
            request.amount(),
            fee,
            fromAccountBalanceAfterTransaction,
            toAccountNumber
        );
        Transaction savedFromTransaction = transactionRepository.save(fromTransaction);

        Transaction toTransaction = Transaction.createTransferIn(
            toAccount,
            request.amount(),
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

        // 전체 거래 내역 조회 (fetch join)
        List<Transaction> transactions = transactionRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);

        // 전체 거래 수 조회
        long total = transactionRepository.countByAccountNumber(accountNumber);

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());

        // 페이징된 거래 내역을 DTO로 변환
        List<TransactionHistoryResponse> content = transactions.subList(start, end).stream()
            .map(transaction -> TransactionHistoryResponse.from(TransactionResponse.from(transaction)))
            .toList();

        return new PageImpl<>(content, pageable, total);
    }
}