package com.example.demo.account.controller;

import com.example.demo.account.dto.AccountCreateRequest;
import com.example.demo.account.dto.AccountResponse;
import com.example.demo.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/accounts")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreateRequest request) {
        System.out.println("request = " + request);
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/accounts/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
} 