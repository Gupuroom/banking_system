package com.example.demo.transaction.validation;

import com.example.demo.account.entity.Account;
import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.validation.AccountValidator;
import com.example.demo.common.error.BusinessException;
import com.example.demo.common.error.CommonErrorCode;
import com.example.demo.common.validation.CommonValidator;
import com.example.demo.transaction.type.TransactionType;
import com.example.demo.transaction.error.TransactionErrorCode;
import com.example.demo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorTest {

    @Mock
    private CommonValidator commonValidator;

    @Mock
    private AccountValidator accountValidator;

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionValidator transactionValidator;

    private static final String TEST_ACCOUNT_NUMBER = "13-12-123456";
    private static final BigDecimal TEST_DEPOSIT_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal TEST_DECIMAL_AMOUNT = new BigDecimal("10000.5");
    private static final BigDecimal TEST_ZERO_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal TEST_NEGATIVE_AMOUNT = new BigDecimal("-10000");
    private static final BigDecimal TEST_LARGE_AMOUNT = new BigDecimal("20000000");
    private static final BigDecimal TEST_WITHDRAWAL_AMOUNT = new BigDecimal("50000");

    private Account testAccount;

    @BeforeEach
    void setUp() {
        transactionValidator = new TransactionValidator(commonValidator, accountValidator, transactionRepository);
        testAccount = Account.create(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("입금 검증")
    class DepositValidation {
        @Test
        @DisplayName("입금 검증 성공")
        void validateDeposit_success() {
            // given
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);

            // when & then
            transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            verify(commonValidator).validateAmountFormat(TEST_DEPOSIT_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_DEPOSIT_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(testAccount);
        }

        @Test
        @DisplayName("null 금액으로 입금 시도 시 실패")
        void validateDeposit_nullAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validateAmountFormat(null);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(null);
            verify(commonValidator, never()).validatePositiveAmount(any());
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
        }

        @Test
        @DisplayName("소수점이 있는 금액으로 입금 시도 시 실패")
        void validateDeposit_decimalAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validateAmountFormat(TEST_DECIMAL_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DECIMAL_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(TEST_DECIMAL_AMOUNT);
            verify(commonValidator, never()).validatePositiveAmount(any());
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
        }

        @Test
        @DisplayName("0원으로 입금 시도 시 실패")
        void validateDeposit_zeroAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validatePositiveAmount(TEST_ZERO_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_ZERO_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(TEST_ZERO_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_ZERO_AMOUNT);
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
        }

        @Test
        @DisplayName("음수 금액으로 입금 시도 시 실패")
        void validateDeposit_negativeAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validatePositiveAmount(TEST_NEGATIVE_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_NEGATIVE_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(TEST_NEGATIVE_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_NEGATIVE_AMOUNT);
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
        }

        @Test
        @DisplayName("존재하지 않는 계좌로 입금 시도 시 실패")
        void validateDeposit_accountNotFound() {
            // given
            doThrow(new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND))
                .when(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_NOT_FOUND);

            verify(commonValidator).validateAmountFormat(TEST_DEPOSIT_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_DEPOSIT_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator, never()).validateAccountStatus(any());
        }

        @Test
        @DisplayName("삭제된 계좌로 입금 시도 시 실패")
        void validateDeposit_accountDeleted() {
            // given
            Account deletedAccount = Account.create(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
            deletedAccount.delete();
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(deletedAccount);
            doThrow(new BusinessException(AccountErrorCode.INVALID_ACCOUNT_STATUS))
                .when(accountValidator).validateAccountStatus(deletedAccount);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.INVALID_ACCOUNT_STATUS);

            verify(commonValidator).validateAmountFormat(TEST_DEPOSIT_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_DEPOSIT_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(deletedAccount);
        }

        @Test
        @DisplayName("최대 금액을 초과하는 금액으로 입금 시도 시 실패")
        void validateDeposit_amountTooLarge() {
            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_LARGE_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.AMOUNT_TOO_LARGE);

            verify(commonValidator).validateAmountFormat(TEST_LARGE_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_LARGE_AMOUNT);
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
        }
    }

    @Nested
    @DisplayName("출금 검증")
    class WithdrawalValidation {
        @Test
        @DisplayName("출금 검증 성공")
        void validateWithdrawal_success() {
            // given
            testAccount = Account.create(TEST_ACCOUNT_NUMBER, new BigDecimal("100000"));
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);
            when(transactionRepository.getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.WITHDRAWAL), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

            // when & then
            transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_WITHDRAWAL_AMOUNT);

            verify(commonValidator).validateAmountFormat(TEST_WITHDRAWAL_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_WITHDRAWAL_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(testAccount);
            verify(transactionRepository).getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.WITHDRAWAL), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("null 금액으로 출금 시도 시 실패")
        void validateWithdrawal_nullAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validateAmountFormat(null);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(null);
            verify(commonValidator, never()).validatePositiveAmount(any());
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
            verify(transactionRepository, never()).getDailyTransactionAmount(any(), any(), any(), any());
        }

        @Test
        @DisplayName("소수점이 있는 금액으로 출금 시도 시 실패")
        void validateWithdrawal_decimalAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validateAmountFormat(TEST_DECIMAL_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DECIMAL_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(TEST_DECIMAL_AMOUNT);
            verify(commonValidator, never()).validatePositiveAmount(any());
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
            verify(transactionRepository, never()).getDailyTransactionAmount(any(), any(), any(), any());
        }

        @Test
        @DisplayName("0원으로 출금 시도 시 실패")
        void validateWithdrawal_zeroAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validatePositiveAmount(TEST_ZERO_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_ZERO_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(TEST_ZERO_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_ZERO_AMOUNT);
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
            verify(transactionRepository, never()).getDailyTransactionAmount(any(), any(), any(), any());
        }

        @Test
        @DisplayName("음수 금액으로 출금 시도 시 실패")
        void validateWithdrawal_negativeAmount() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validatePositiveAmount(TEST_NEGATIVE_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_NEGATIVE_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(TEST_NEGATIVE_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_NEGATIVE_AMOUNT);
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
            verify(transactionRepository, never()).getDailyTransactionAmount(any(), any(), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 계좌로 출금 시도 시 실패")
        void validateWithdrawal_accountNotFound() {
            // given
            doThrow(new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND))
                .when(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_WITHDRAWAL_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.ACCOUNT_NOT_FOUND);

            verify(commonValidator).validateAmountFormat(TEST_WITHDRAWAL_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_WITHDRAWAL_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator, never()).validateAccountStatus(any());
            verify(transactionRepository, never()).getDailyTransactionAmount(any(), any(), any(), any());
        }

        @Test
        @DisplayName("삭제된 계좌로 출금 시도 시 실패")
        void validateWithdrawal_accountDeleted() {
            // given
            Account deletedAccount = Account.create(TEST_ACCOUNT_NUMBER, new BigDecimal("100000"));
            deletedAccount.delete();
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(deletedAccount);
            doThrow(new BusinessException(AccountErrorCode.INVALID_ACCOUNT_STATUS))
                .when(accountValidator).validateAccountStatus(deletedAccount);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_WITHDRAWAL_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorCode.INVALID_ACCOUNT_STATUS);

            verify(commonValidator).validateAmountFormat(TEST_WITHDRAWAL_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_WITHDRAWAL_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(deletedAccount);
            verify(transactionRepository, never()).getDailyTransactionAmount(any(), any(), any(), any());
        }

        @Test
        @DisplayName("잔액 부족으로 출금 시도 시 실패")
        void validateWithdrawal_insufficientBalance() {
            // given
            testAccount = Account.create(TEST_ACCOUNT_NUMBER, new BigDecimal("10000"));
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_WITHDRAWAL_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.INSUFFICIENT_BALANCE);

            verify(commonValidator).validateAmountFormat(TEST_WITHDRAWAL_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_WITHDRAWAL_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(testAccount);
            verify(transactionRepository, never()).getDailyTransactionAmount(any(), any(), any(), any());
        }

        @Test
        @DisplayName("일일 출금 한도 초과로 출금 시도 시 실패")
        void validateWithdrawal_dailyLimitExceeded() {
            // given
            testAccount = Account.create(TEST_ACCOUNT_NUMBER, new BigDecimal("100000"));
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);
            when(transactionRepository.getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.WITHDRAWAL), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("960000"));

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_WITHDRAWAL_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);

            verify(commonValidator).validateAmountFormat(TEST_WITHDRAWAL_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_WITHDRAWAL_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(testAccount);
            verify(transactionRepository).getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.WITHDRAWAL), any(LocalDateTime.class), any(LocalDateTime.class));
        }
    }
} 