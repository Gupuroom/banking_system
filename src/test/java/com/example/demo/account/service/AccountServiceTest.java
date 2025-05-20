package com.example.demo.account.service;

import com.example.demo.account.dto.AccountCreateRequest;
import com.example.demo.account.dto.AccountResponse;
import com.example.demo.account.entity.Account;
import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.account.type.AccountStatus;
import com.example.demo.common.error.BusinessException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Nested
    @DisplayName("계좌 생성")
    class CreateAccount {
        private final String VALID_ACCOUNT_NUMBER = "1234567890";
        private final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

        @Test
        @DisplayName("계좌 생성 성공")
        void createAccountSuccess() {
            // given
            AccountCreateRequest request = new AccountCreateRequest(VALID_ACCOUNT_NUMBER, INITIAL_BALANCE);
            Account account = Account.create(VALID_ACCOUNT_NUMBER, INITIAL_BALANCE);
            
            given(accountRepository.existsByAccountNumber(VALID_ACCOUNT_NUMBER)).willReturn(false);
            given(accountRepository.save(any(Account.class))).willReturn(account);

            // when
            AccountResponse response = accountService.createAccount(request);

            // then
            assertThat(response.accountNumber()).isEqualTo(VALID_ACCOUNT_NUMBER);
            assertThat(response.balance()).isEqualTo(INITIAL_BALANCE);
            assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("이미 존재하는 계좌번호로 생성 시도시 실패")
        void createAccountWithExistingAccountNumber() {
            // given
            AccountCreateRequest request = new AccountCreateRequest(VALID_ACCOUNT_NUMBER, INITIAL_BALANCE);
            given(accountRepository.existsByAccountNumber(VALID_ACCOUNT_NUMBER)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("계좌 삭제")
    class DeleteAccount {
        private final String VALID_ACCOUNT_NUMBER = "1234567890";

        @Test
        @DisplayName("계좌 삭제 성공")
        void deleteAccountSuccess() {
            // given
            Account account = Account.create(VALID_ACCOUNT_NUMBER, BigDecimal.ZERO);
            given(accountRepository.findByAccountNumber(VALID_ACCOUNT_NUMBER))
                    .willReturn(Optional.of(account));

            // when
            accountService.deleteAccount(VALID_ACCOUNT_NUMBER);

            // then
            assertThat(account.getStatus()).isEqualTo(AccountStatus.DELETED);
            verify(accountRepository).findByAccountNumber(VALID_ACCOUNT_NUMBER);
        }

        @Test
        @DisplayName("존재하지 않는 계좌 삭제 시도시 실패")
        void deleteNonExistentAccount() {
            // given
            given(accountRepository.findByAccountNumber(VALID_ACCOUNT_NUMBER))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(VALID_ACCOUNT_NUMBER))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        @Test
        @DisplayName("이미 삭제된 계좌 삭제 시도시 실패")
        void deleteAlreadyDeletedAccount() {
            // given
            Account account = Account.create(VALID_ACCOUNT_NUMBER, BigDecimal.ZERO);
            account.delete(); // 계좌 상태를 DELETED로 변경
            given(accountRepository.findByAccountNumber(VALID_ACCOUNT_NUMBER))
                    .willReturn(Optional.of(account));

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(VALID_ACCOUNT_NUMBER))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_ALREADY_DELETED);
        }
    }
} 