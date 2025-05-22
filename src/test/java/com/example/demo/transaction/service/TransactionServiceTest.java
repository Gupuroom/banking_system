package com.example.demo.transaction.service;

import com.example.demo.account.entity.Account;
import com.example.demo.account.entity.AccountType;
import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.account.type.AccountStatus;
import com.example.banking.core.error.BusinessException;
import com.example.banking.core.error.CommonErrorCode;
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
import org.mockito.InjectMocks;
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

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionValidator transactionValidator;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private Account normalAccount;
    private Account premiumAccount;
    private Account vipAccount;
    private AccountType normalType;
    private AccountType premiumType;
    private AccountType vipType;

    private static final String TEST_ACCOUNT_NUMBER = "13-12-123456";
    private static final BigDecimal TEST_INITIAL_BALANCE = new BigDecimal("100000");
    private static final BigDecimal TEST_DEPOSIT_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal TEST_DECIMAL_AMOUNT = new BigDecimal("10000.5");
    private static final BigDecimal TEST_ZERO_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal TEST_NEGATIVE_AMOUNT = new BigDecimal("-10000");
    private static final BigDecimal TEST_LARGE_AMOUNT = new BigDecimal("20000000");

    @BeforeEach
    void setUp() {
        // 계좌 타입 설정
        normalType = AccountType.builder()
            .code("NORMAL")
            .description("일반계좌")
            .transferFeeRate(new BigDecimal("0.01"))
            .dailyWithdrawalLimit(new BigDecimal("1000000"))
            .dailyTransferLimit(new BigDecimal("3000000"))
            .build();

        premiumType = AccountType.builder()
            .code("PREMIUM")
            .description("프리미엄계좌")
            .transferFeeRate(new BigDecimal("0.005"))
            .dailyWithdrawalLimit(new BigDecimal("5000000"))
            .dailyTransferLimit(new BigDecimal("10000000"))
            .build();

        vipType = AccountType.builder()
            .code("VIP")
            .description("VIP계좌")
            .transferFeeRate(BigDecimal.ZERO)
            .dailyWithdrawalLimit(new BigDecimal("10000000"))
            .dailyTransferLimit(new BigDecimal("20000000"))
            .build();

        // 계좌 설정
        normalAccount = Account.builder()
            .accountNumber("1234567891")
            .balance(new BigDecimal("1000000"))
            .accountType(normalType)
            .status(AccountStatus.ACTIVE)
            .build();

        premiumAccount = Account.builder()
            .accountNumber("2345678910")
            .balance(new BigDecimal("1000000"))
            .accountType(premiumType)
            .status(AccountStatus.ACTIVE)
            .build();

        vipAccount = Account.builder()
            .accountNumber("3456789012")
            .balance(new BigDecimal("1000000"))
            .accountType(vipType)
            .status(AccountStatus.ACTIVE)
            .build();
    }

    @Nested
    @DisplayName("입금 테스트")
    class DepositTest {
        @Test
        @DisplayName("입금 성공")
        void deposit_success() {
            // given
            String accountNumber = "1234567891";
            BigDecimal amount = new BigDecimal("100000");
            TransactionRequest request = new TransactionRequest(amount);

            when(accountRepository.findByAccountNumberWithLock(accountNumber))
                .thenReturn(Optional.of(normalAccount));
            when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            TransactionResponse response = transactionService.deposit(accountNumber, request);

            // then
            assertThat(response.amount()).isEqualTo(amount);
            assertThat(response.type()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(response.fee()).isEqualTo(BigDecimal.ZERO);
            assertThat(normalAccount.getBalance()).isEqualTo(new BigDecimal("1100000"));

            verify(transactionValidator).validateDeposit(accountNumber, amount);
            verify(accountRepository).findByAccountNumberWithLock(accountNumber);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("입금 실패 - 검증 실패")
        void deposit_validation_failure() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateDeposit(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository, never()).findByAccountNumberWithLock(any());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("출금 테스트")
    class WithdrawalTest {
        @Test
        @DisplayName("출금 성공")
        void withdraw_success() {
            // given
            String accountNumber = "1234567891";
            BigDecimal amount = new BigDecimal("100000");
            TransactionRequest request = new TransactionRequest(amount);

            when(accountRepository.findByAccountNumberWithLock(accountNumber))
                .thenReturn(Optional.of(normalAccount));
            when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            TransactionResponse response = transactionService.withdraw(accountNumber, request);

            // then
            assertThat(response.amount()).isEqualTo(amount);
            assertThat(response.type()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(response.fee()).isEqualTo(BigDecimal.ZERO);
            assertThat(normalAccount.getBalance()).isEqualTo(new BigDecimal("900000"));

            verify(transactionValidator).validateWithdrawal(accountNumber, amount);
            verify(accountRepository).findByAccountNumberWithLock(accountNumber);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("출금 실패 - 검증 실패")
        void withdraw_validation_failure() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateWithdrawal(TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository, never()).findByAccountNumberWithLock(any());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("이체 테스트")
    class TransferTest {
        @Test
        @DisplayName("이체 성공")
        void transfer_normalToNormal_success() {
            // given
            String fromAccountNumber = "1234567891";
            String toAccountNumber = "2345678910";
            BigDecimal amount = new BigDecimal("100000");
            TransactionRequest request = new TransactionRequest(amount);

            Account fromAccount = Account.builder()
                .accountNumber(fromAccountNumber)
                .balance(new BigDecimal("1000000"))
                .accountType(normalType)
                .status(AccountStatus.ACTIVE)
                .build();

            Account toAccount = Account.builder()
                .accountNumber(toAccountNumber)
                .balance(new BigDecimal("1000000"))
                .accountType(normalType)
                .status(AccountStatus.ACTIVE)
                .build();

            when(accountRepository.findByAccountNumberWithLock(fromAccountNumber))
                .thenReturn(Optional.of(fromAccount));
            when(accountRepository.findByAccountNumberWithLock(toAccountNumber))
                .thenReturn(Optional.of(toAccount));
            when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            TransactionResponse response = transactionService.transfer(fromAccountNumber, toAccountNumber, request);

            // then
            // 수수료 계산 확인 (1%)
            BigDecimal expectedFee = new BigDecimal("1000"); // 100,000 * 0.01
            BigDecimal expectedFromBalance = new BigDecimal("899000"); // 1,000,000 - 100,000 - 1,000
            BigDecimal expectedToBalance = new BigDecimal("1100000"); // 1,000,000 + 100,000

            assertThat(response.amount()).isEqualTo(amount);
            assertThat(response.type()).isEqualTo(TransactionType.TRANSFER_OUT);
            assertThat(response.fee()).isEqualTo(expectedFee);
            assertThat(fromAccount.getBalance()).isEqualTo(expectedFromBalance);
            assertThat(toAccount.getBalance()).isEqualTo(expectedToBalance);

            // 거래 내역 저장 확인 (출금 계좌와 입금 계좌 모두)
            verify(transactionRepository, times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("이체 실패 - 검증 실패")
        void transfer_validation_failure() {
            // given
            TransactionRequest request = new TransactionRequest(TEST_DEPOSIT_AMOUNT);
            doThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE))
                .when(transactionValidator).validateTransfer(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);

            // when & then
            assertThatThrownBy(() -> transactionService.transfer(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_NUMBER, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);

            verify(transactionValidator).validateTransfer(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_NUMBER, TEST_DEPOSIT_AMOUNT);
            verify(accountRepository, never()).findByAccountNumberWithLock(any());
            verify(transactionRepository, never()).save(any());
        }
    }
}