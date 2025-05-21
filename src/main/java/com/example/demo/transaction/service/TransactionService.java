package com.example.demo.transaction.service;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.transaction.dto.TransactionRequest;
import com.example.demo.transaction.dto.TransactionResponse;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import com.example.demo.transaction.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final AccountRepository accountRepository;
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
}