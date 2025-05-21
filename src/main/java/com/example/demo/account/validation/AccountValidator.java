package com.example.demo.account.validation;

import com.example.demo.account.entity.Account;
import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.account.type.AccountStatus;
import com.example.demo.common.error.BusinessException;
import com.example.demo.common.validation.CommonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AccountValidator {
    private final AccountRepository accountRepository;
    private final CommonValidator commonValidator;

    public Account validateAccountExists(String accountNumber) {
        commonValidator.validateAccountNumberFormat(accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    public void validateAccountStatus(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(AccountErrorCode.INVALID_ACCOUNT_STATUS);
        }
    }
}