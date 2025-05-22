package com.example.banking.domain.account.service;

import com.example.banking.core.error.BusinessException;
import com.example.banking.domain.account.dto.AccountResponse;
import com.example.banking.domain.account.entity.Account;
import com.example.banking.domain.account.entity.AccountType;
import com.example.banking.domain.account.error.AccountErrorCode;
import com.example.banking.domain.account.repository.AccountRepository;
import com.example.banking.domain.account.repository.AccountTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;

    @Transactional
    public AccountResponse createAccount(String accountNumber, BigDecimal initialBalance) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_ALREADY_EXISTS);
        }

        // 기본 type 은 1이라고 가정한다. data.sql 참고
        AccountType accountType = accountTypeRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_TYPE_NOT_FOUND));

        Account account = Account.create(accountNumber, initialBalance, accountType);
        Account savedAccount = accountRepository.save(account);

        return AccountResponse.from(savedAccount);
    }


    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        account.delete();
    }
}