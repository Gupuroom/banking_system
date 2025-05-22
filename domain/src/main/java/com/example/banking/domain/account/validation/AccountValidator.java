package com.example.banking.domain.account.validation;

import com.example.banking.core.error.BusinessException;
import com.example.banking.core.validation.CommonValidator;
import com.example.banking.domain.account.entity.Account;
import com.example.banking.domain.account.error.AccountErrorCode;
import com.example.banking.domain.account.repository.AccountRepository;
import com.example.banking.domain.account.type.AccountStatus;
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