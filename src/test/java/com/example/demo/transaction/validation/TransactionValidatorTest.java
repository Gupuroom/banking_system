package com.example.demo.transaction.validation;

import com.example.demo.account.entity.Account;
import com.example.demo.account.entity.AccountType;
import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.account.type.AccountStatus;
import com.example.demo.account.validation.AccountValidator;
import com.example.banking.core.error.BusinessException;
import com.example.banking.core.error.CommonErrorCode;
import com.example.banking.core.validation.CommonValidator;
import com.example.demo.transaction.error.TransactionErrorCode;
import com.example.demo.transaction.repository.TransactionRepository;
import com.example.demo.transaction.type.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorTest {

    @InjectMocks
    private TransactionValidator transactionValidator;

    @Mock
    private CommonValidator commonValidator;

    @Mock
    private AccountValidator accountValidator;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    private static final String TEST_ACCOUNT_NUMBER = "1234567891";
    private static final String TEST_TO_ACCOUNT_NUMBER = "2345678910";
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("50000");

    private Account testAccount;
    private Account testToAccount;

    private AccountType normalType;

    @BeforeEach
    void setUp() {
        transactionValidator = new TransactionValidator(commonValidator, accountValidator, transactionRepository);
        
        // 일반계좌 타입 설정
        normalType = AccountType.builder()
            .code("NORMAL")
            .description("일반계좌")
            .transferFeeRate(new BigDecimal("0.01"))
            .dailyWithdrawalLimit(new BigDecimal("1000000"))
            .dailyTransferLimit(new BigDecimal("3000000"))
            .build();

        // 테스트용 일반계좌 설정 (잔액 5,000,000원으로 설정)
        testAccount = Account.builder()
            .accountNumber(TEST_ACCOUNT_NUMBER)
            .balance(new BigDecimal("5000000"))
            .accountType(normalType)
            .status(AccountStatus.ACTIVE)
            .build();

        testToAccount = Account.builder()
            .accountNumber(TEST_TO_ACCOUNT_NUMBER)
            .balance(new BigDecimal("1000000"))
            .accountType(normalType)
            .status(AccountStatus.ACTIVE)
            .build();
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
            transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_AMOUNT);

            verify(commonValidator).validateAmountFormat(TEST_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(testAccount);
        }

        @Test
        @DisplayName("입금 검증 실패 - 금액 형식 오류")
        void validateDeposit_amountFormatError() {
            // given
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(commonValidator).validateAmountFormat(TEST_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, TEST_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(commonValidator).validateAmountFormat(TEST_AMOUNT);
            verify(commonValidator, never()).validatePositiveAmount(any());
            verify(accountValidator, never()).validateAccountExists(any());
            verify(accountValidator, never()).validateAccountStatus(any());
        }

        @Test
        @DisplayName("입금 검증 실패 - 최대 금액 초과")
        void validateDeposit_amountTooLarge() {
            // given
            BigDecimal amount = new BigDecimal("21000000"); // 최대 금액 2천만원 초과

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateDeposit(TEST_ACCOUNT_NUMBER, amount))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.AMOUNT_TOO_LARGE);

            verify(commonValidator).validateAmountFormat(amount);
            verify(commonValidator).validatePositiveAmount(amount);
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
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);
            when(transactionRepository.getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.WITHDRAWAL), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

            // when & then
            transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_AMOUNT);

            verify(commonValidator).validateAmountFormat(TEST_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(testAccount);
        }

        @Test
        @DisplayName("출금 검증 실패 - 잔액 부족")
        void validateWithdrawal_insufficientBalance() {
            // given
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, new BigDecimal("6000000")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("출금 검증 실패 - 일일 한도 초과")
        void validateWithdrawal_dailyLimitExceeded() {
            // given
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);
            when(transactionRepository.getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.WITHDRAWAL), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("600000")); // 이미 60만원 출금

            // when & then
            assertThatThrownBy(() -> transactionValidator.validateWithdrawal(TEST_ACCOUNT_NUMBER, new BigDecimal("500000")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("이체 검증")
    class TransferValidation {
        @Test
        @DisplayName("이체 검증 성공")
        void validateTransfer_success() {
            // given
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);
            when(accountValidator.validateAccountExists(TEST_TO_ACCOUNT_NUMBER))
                .thenReturn(testToAccount);
            when(transactionRepository.getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.TRANSFER_OUT), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

            // when & then
            transactionValidator.validateTransfer(TEST_ACCOUNT_NUMBER, TEST_TO_ACCOUNT_NUMBER, TEST_AMOUNT);

            verify(commonValidator).validateAmountFormat(TEST_AMOUNT);
            verify(commonValidator).validatePositiveAmount(TEST_AMOUNT);
            verify(accountValidator).validateAccountExists(TEST_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountExists(TEST_TO_ACCOUNT_NUMBER);
            verify(accountValidator).validateAccountStatus(testAccount);
            verify(accountValidator).validateAccountStatus(testToAccount);
        }

        @Test
        @DisplayName("이체 검증 실패 - 자기 계좌로 이체")
        void validateTransfer_sameAccount() {
            // when & then
            assertThatThrownBy(() -> transactionValidator.validateTransfer(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_NUMBER, TEST_AMOUNT))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.SAME_ACCOUNT_TRANSFER);
        }

        @Test
        @DisplayName("이체 검증 실패 - 수수료 포함 잔액 부족")
        void validateTransfer_insufficientBalanceWithFee() {
            // given
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);
            when(accountValidator.validateAccountExists(TEST_TO_ACCOUNT_NUMBER))
                .thenReturn(testToAccount);

            // 4,960,000원 이체 시도
            // 수수료 1%: 49,600원
            // 총 필요 금액: 5,009,600원
            // 계좌 잔액 500만원으로는 부족
            assertThatThrownBy(() -> transactionValidator.validateTransfer(TEST_ACCOUNT_NUMBER, TEST_TO_ACCOUNT_NUMBER, new BigDecimal("4960000")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("이체 검증 실패 - 일일 한도 초과")
        void validateTransfer_dailyLimitExceeded() {
            // given
            when(accountValidator.validateAccountExists(TEST_ACCOUNT_NUMBER))
                .thenReturn(testAccount);
            when(accountValidator.validateAccountExists(TEST_TO_ACCOUNT_NUMBER))
                .thenReturn(testToAccount);
            when(transactionRepository.getDailyTransactionAmount(
                any(Account.class), eq(TransactionType.TRANSFER_OUT), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("2000000")); // 이미 200만원 이체

            // when & then
            // 150만원 이체 시도 (일일 한도 300만원 초과)
            assertThatThrownBy(() -> transactionValidator.validateTransfer(TEST_ACCOUNT_NUMBER, TEST_TO_ACCOUNT_NUMBER, new BigDecimal("1500000")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED);
        }
    }
} 