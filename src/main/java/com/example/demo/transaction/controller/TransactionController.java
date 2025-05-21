package com.example.demo.transaction.controller;

import com.example.demo.transaction.dto.TransactionHistoryResponse;
import com.example.demo.transaction.dto.TransactionRequest;
import com.example.demo.transaction.dto.TransactionResponse;
import com.example.demo.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transaction", description = "거래 API")
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "입금", description = "계좌에 입금을 수행합니다.")
    @PostMapping("/api/accounts/{accountNumber}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
        @PathVariable String accountNumber,
        @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.deposit(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "출금", description = "계좌에서 출금을 수행합니다.")
    @PostMapping("/api/accounts/{accountNumber}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
        @PathVariable String accountNumber,
        @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.withdraw(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이체", description = "한 계좌에서 다른 계좌로 이체를 수행합니다.")
    @PostMapping("/api/accounts/{fromAccountNumber}/transfer/{toAccountNumber}")
    public ResponseEntity<TransactionResponse> transfer(
        @PathVariable String fromAccountNumber,
        @PathVariable String toAccountNumber,
        @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.transfer(fromAccountNumber, toAccountNumber, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 조회", description = "계좌의 거래 내역을 조회합니다.")
    @GetMapping("/api/accounts/{accountNumber}/transactions")
    public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
        @PathVariable String accountNumber,
        Pageable pageable
    ) {
        Page<TransactionHistoryResponse> history = transactionService.getTransactionHistory(accountNumber, pageable);
        return ResponseEntity.ok(history);
    }
} 