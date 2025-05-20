package com.example.demo.account.service;

import com.example.demo.account.dto.AccountCreateRequest;
import com.example.demo.account.dto.AccountResponse;
import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        Account account = Account.create(request.accountNumber(), request.initialBalance());
        Account savedAccount = accountRepository.save(account);

        return AccountResponse.from(savedAccount);
    }


    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(NullPointerException::new);

        account.delete();
    }
}