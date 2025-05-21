package com.example.demo.transaction.service;

import com.example.demo.account.entity.Account;
import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.common.error.BusinessException;
import com.example.demo.common.error.CommonErrorCode;
import com.example.demo.transaction.dto.TransactionRequest;
import com.example.demo.transaction.dto.TransactionResponse;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.error.TransactionErrorCode;
import com.example.demo.transaction.repository.TransactionRepository;
import com.example.demo.transaction.type.TransactionType;
import com.example.demo.transaction.validation.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionValidator transactionValidator;

    private TransactionService transactionService;

    private static final String TEST_ACCOUNT_NUMBER = "13-12-123456";
    private static final BigDecimal TEST_INITIAL_BALANCE = new BigDecimal("100000");
    private static final BigDecimal TEST_DEPOSIT_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal TEST_DECIMAL_AMOUNT = new BigDecimal("10000.5");
    private static final BigDecimal TEST_ZERO_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal TEST_NEGATIVE_AMOUNT = new BigDecimal("-10000");
    private static final BigDecimal TEST_LARGE_AMOUNT = new BigDecimal("20000000");

    private Account testAccount;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(accountRepository, transactionRepository, transactionValidator);
        testAccount = Account.create(TEST_ACCOUNT_NUMBER, TEST_INITIAL_BALANCE);
    }

    @Nested
    @DisplayName("입금")
    class Deposit {
        @Test
        @DisplayName("입금 성공")
        void deposit_success() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            Transaction transaction = Transaction.createDeposit(testAccount, TEST_DEPOSIT_AMOUNT);
            when(accountRepository.findByAccountNumber(TEST_ACCOUNT_NUMBER))
                .thenReturn(Optional.of(testAccount));
            when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

            // when
            TransactionResponse response = transactionService.deposit(TEST_ACCOUNT_NUMBER, request);

            // then
            assertThat(response.accountNumber()).isEqualTo(TEST_ACCOUNT_NUMBER);
            assertThat(response.amount()).isEqualTo(TEST_DEPOSIT_AMOUNT);
            assertThat(response.type()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(testAccount.getBalance()).isEqualTo(TEST_INITIAL_BALANCE.add(TEST_DEPOSIT_AMOUNT));

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository).findByAccountNumber(TEST_ACCOUNT_NUMBER);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("null 금액으로 입금 시도 시 실패")
        void deposit_nullAmount() {
            // given
            TransactionRequest request = new TransactionRequest(null);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, null);

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, null);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("소수점이 있는 금액으로 입금 시도 시 실패")
        void deposit_decimalAmount() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DECIMAL_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DECIMAL_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DECIMAL_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("0원으로 입금 시도 시 실패")
        void deposit_zeroAmount() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_ZERO_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_ZERO_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_ZERO_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("음수 금액으로 입금 시도 시 실패")
        void deposit_negativeAmount() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_NEGATIVE_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_NEGATIVE_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_NEGATIVE_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 계좌로 입금 시도 시 실패")
        void deposit_accountNotFound() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            doThrow(new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND))
                .when(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_NOT_FOUND);

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("최대 금액을 초과하는 금액으로 입금 시도 시 실패")
        void deposit_amountTooLarge() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_LARGE_AMOUNT);
            doThrow(new BusinessException(TransactionErrorCode.AMOUNT_TOO_LARGE))
                .when(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_LARGE_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.AMOUNT_TOO_LARGE);

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_LARGE_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("출금")
    class Withdrawal {
        @Test
        @DisplayName("출금 성공")
        void withdraw_success() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            Transaction transaction = Transaction.createWithdrawal(testAccount, TEST_DEPOSIT_AMOUNT);
            when(accountRepository.findByAccountNumber(TEST_ACCOUNT_NUMBER))
                .thenReturn(Optional.of(testAccount));
            when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

            // when
            TransactionResponse response = transactionService.withdraw(TEST_ACCOUNT_NUMBER, request);

            // then
            assertThat(response.accountNumber()).isEqualTo(TEST_ACCOUNT_NUMBER);
            assertThat(response.amount()).isEqualTo(TEST_DEPOSIT_AMOUNT);
            assertThat(response.type()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(testAccount.getBalance()).isEqualTo(TEST_INITIAL_BALANCE.subtract(TEST_DEPOSIT_AMOUNT));

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository).findByAccountNumber(TEST_ACCOUNT_NUMBER);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("null 금액으로 출금 시도 시 실패")
        void withdraw_nullAmount() {
            // given
            TransactionRequest request = new TransactionRequest(null);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, null);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, null);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("소수점이 있는 금액으로 출금 시도 시 실패")
        void withdraw_decimalAmount() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DECIMAL_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DECIMAL_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DECIMAL_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("0원으로 출금 시도 시 실패")
        void withdraw_zeroAmount() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_ZERO_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_ZERO_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_ZERO_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("음수 금액으로 출금 시도 시 실패")
        void withdraw_negativeAmount() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_NEGATIVE_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_NEGATIVE_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_NEGATIVE_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 계좌로 출금 시도 시 실패")
        void withdraw_accountNotFound() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            doThrow(new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_NOT_FOUND);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("잔액 부족으로 출금 시도 시 실패")
        void withdraw_insufficientBalance() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            doThrow(new BusinessException(TransactionErrorCode.INSUFFICIENT_BALANCE))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.INSUFFICIENT_BALANCE);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("일일 출금 한도 초과로 출금 시도 시 실패")
        void withdraw_dailyLimitExceeded() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            doThrow(new BusinessException(TransactionErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository, never()).findByAccountNumber(any());
            verify(transactionRepository, never()).save(any());
        }
    }
}