package com.example.banking.api.account.controller;

import com.example.banking.api.account.dto.AccountCreateRequest;
import com.example.banking.domain.account.dto.AccountResponse;
import com.example.banking.domain.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "계좌 관리", description = "계좌 생성, 조회, 삭제 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계좌 생성", description = "새로운 계좌를 생성합니다.")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse response = accountService.createAccount(request.accountNumber(), request.initialBalance());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계좌 삭제", description = "계좌를 삭제 처리합니다.")
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.ok().build();
    }
} 