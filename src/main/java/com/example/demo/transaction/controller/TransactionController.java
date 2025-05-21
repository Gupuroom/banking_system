package com.example.demo.transaction.controller;

import com.example.demo.transaction.dto.TransactionRequest;
import com.example.demo.transaction.dto.TransactionResponse;
import com.example.demo.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
} 