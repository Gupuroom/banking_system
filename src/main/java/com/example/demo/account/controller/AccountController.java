package com.example.demo.account.controller;

import com.example.banking.domain.account.dto.AccountResponse;
import com.example.banking.domain.account.service.AccountService;
import com.example.demo.account.dto.AccountCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Account", description = "계좌 API")
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계좌 생성", description = "새로운 계좌를 생성합니다.")
    @PostMapping("/api/accounts")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreateRequest request) {
        AccountResponse response = accountService.createAccount(request.accountNumber(), request.initialBalance());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계좌 삭제", description = "계좌를 삭제합니다.")
    @DeleteMapping("/api/accounts/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
} 