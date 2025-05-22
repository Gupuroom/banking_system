package com.example.banking.domain.account.validation;

import com.example.banking.core.error.BusinessException;
import com.example.banking.core.validation.CommonValidator;
import com.example.banking.domain.account.entity.Account;
import com.example.banking.domain.account.error.AccountErrorCode;
import com.example.banking.domain.account.repository.AccountRepository;
import com.example.banking.domain.account.type.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountValidatorTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CommonValidator commonValidator;

    @InjectMocks
    private AccountValidator validator;

    private Account activeAccount;
    private Account deletedAccount;

    @BeforeEach
    void setUp() {
        activeAccount = Account.builder()
            .accountNumber("13-12-123456")
            .balance(new BigDecimal("10000"))
            .status(AccountStatus.ACTIVE)
            .build();

        deletedAccount = Account.builder()
            .accountNumber("13-12-654321")
            .balance(new BigDecimal("10000"))
            .status(AccountStatus.DELETED)
            .build();
    }

    @Nested
    @DisplayName("계좌 존재 여부 검증")
    class AccountExistenceValidation {
        @Test
        @DisplayName("존재하는 계좌 검증 성공")
        void validateAccountExists_success() {
            // given
            String accountNumber = "13-12-123456";
            doNothing().when(commonValidator).validateAccountNumberFormat(accountNumber);
            when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(activeAccount));

            // when
            Account result = validator.validateAccountExists(accountNumber);

            // then
            assertThat(result).isEqualTo(activeAccount);
        }

        @Test
        @DisplayName("존재하지 않는 계좌 검증 실패")
        void validateAccountExists_failure() {
            // given
            String accountNumber = "13-12-999999";
            doNothing().when(commonValidator).validateAccountNumberFormat(accountNumber);
            when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> validator.validateAccountExists(accountNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("계좌 상태 검증")
    class AccountStatusValidation {
        @Test
        @DisplayName("활성 상태 계좌 검증 성공")
        void validateAccountStatus_success() {
            // when & then
            assertThat(activeAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            validator.validateAccountStatus(activeAccount);
        }

        @Test
        @DisplayName("삭제된 계좌 검증 실패")
        void validateAccountStatus_failure() {
            // when & then
            assertThatThrownBy(() -> validator.validateAccountStatus(deletedAccount))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.INVALID_ACCOUNT_STATUS);
        }
    }
} 