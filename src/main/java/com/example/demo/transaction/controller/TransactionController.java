package com.example.demo.transaction.controller;

import com.example.demo.transaction.dto.TransactionHistoryResponse;
import com.example.demo.transaction.dto.TransactionRequest;
import com.example.demo.transaction.dto.TransactionResponse;
import com.example.demo.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/api/accounts/{accountNumber}/deposit")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable String accountNumber, @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.deposit(accountNumber, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/accounts/{accountNumber}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable String accountNumber, @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.withdraw(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/accounts/{fromAccountNumber}/transfer/{toAccountNumber}")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable String fromAccountNumber,
            @PathVariable String toAccountNumber,
            @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.transfer(fromAccountNumber, toAccountNumber, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/accounts/{accountNumber}/transactions")
    public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(@PathVariable String accountNumber, @PageableDefault(size = 20) Pageable pageable) {
        Page<TransactionHistoryResponse> history = transactionService.getTransactionHistory(accountNumber, pageable);
        return ResponseEntity.ok(history);
    }
} 