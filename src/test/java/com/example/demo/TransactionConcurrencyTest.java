package com.example.demo;

import com.example.banking.core.error.BusinessException;
import com.example.banking.domain.account.entity.Account;
import com.example.banking.domain.account.entity.AccountType;
import com.example.banking.domain.account.repository.AccountRepository;
import com.example.banking.domain.account.repository.AccountTypeRepository;
import com.example.banking.domain.account.type.AccountStatus;
import com.example.banking.domain.transaction.entity.Transaction;
import com.example.banking.domain.transaction.repository.TransactionRepository;
import com.example.banking.domain.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TransactionConcurrencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountTypeRepository accountTypeRepository;

    private Account accountA;
    private Account accountB;
    private Account accountC;
    private AccountType normalType;

    @BeforeEach
    @Transactional
    void setUp() {
        // 계좌 타입 설정 및 저장
        normalType = AccountType.builder()
            .code("NORMAL_TEST")
            .description("일반계좌")
            .transferFeeRate(new BigDecimal("0.01"))
            .dailyWithdrawalLimit(new BigDecimal("10000000"))
            .dailyTransferLimit(new BigDecimal("30000000"))
            .build();
        normalType = accountTypeRepository.save(normalType);

        // 계좌 설정
        accountA = Account.builder()
            .accountNumber("1234567891")
            .balance(new BigDecimal("10000000"))
            .accountType(normalType)
            .status(AccountStatus.ACTIVE)
            .build();

        accountB = Account.builder()
            .accountNumber("2345678910")
            .balance(new BigDecimal("10000000"))
            .accountType(normalType)
            .status(AccountStatus.ACTIVE)
            .build();

        accountC = Account.builder()
            .accountNumber("3456789012")
            .balance(new BigDecimal("10000000"))
            .accountType(normalType)
            .status(AccountStatus.ACTIVE)
            .build();

        // 계좌 저장
        accountA = accountRepository.save(accountA);
        accountB = accountRepository.save(accountB);
        accountC = accountRepository.save(accountC);
    }

    @Test
    @DisplayName("동시 이체 시 잔액 정확성 테스트")
    @Transactional
    void concurrentTransferTest() throws InterruptedException {
        // given
        int threadCount = 10;
        int transferCount = 100;
        BigDecimal transferAmount = new BigDecimal("10000");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    for (int j = 0; j < transferCount; j++) {
                        try {
                            transactionService.transfer(accountA.getAccountNumber(), accountB.getAccountNumber(), transferAmount);
                            successCount.incrementAndGet();
                        } catch (BusinessException e) {
                            failCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Account finalAccountA = accountRepository.findByAccountNumber(accountA.getAccountNumber()).orElseThrow();
        Account finalAccountB = accountRepository.findByAccountNumber(accountB.getAccountNumber()).orElseThrow();
        
        // 수수료 계산 (1%)
        BigDecimal totalTransferredAmount = transferAmount.multiply(new BigDecimal(successCount.get()));
        BigDecimal totalFee = totalTransferredAmount.multiply(new BigDecimal("0.01"));
        
        assertThat(finalAccountA.getBalance().setScale(0))
            .isEqualTo(new BigDecimal("10000000").subtract(totalTransferredAmount).subtract(totalFee).setScale(0));
        assertThat(finalAccountB.getBalance().setScale(0))
            .isEqualTo(new BigDecimal("10000000").add(totalTransferredAmount).setScale(0));
        
        // 거래 내역 순서 확인
        List<Transaction> transactions = transactionRepository.findByAccountOrderByIdDesc(accountA);
        assertThat(transactions).isSortedAccordingTo((t1, t2) -> t2.getId().compareTo(t1.getId()));
    }

    @Test
    @DisplayName("동시 입금 시 잔액 정확성 테스트")
    @Transactional
    void concurrentDepositTest() throws InterruptedException {
        // given
        int threadCount = 10;
        int depositCount = 100;
        BigDecimal depositAmount = new BigDecimal("10000");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    for (int j = 0; j < depositCount; j++) {
                        transactionService.deposit(accountA.getAccountNumber(), depositAmount);
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Account finalAccount = accountRepository.findByAccountNumber(accountA.getAccountNumber()).orElseThrow();
        BigDecimal expectedBalance = new BigDecimal("10000000")
            .add(depositAmount.multiply(new BigDecimal(successCount.get())))
            .setScale(0);
        
        assertThat(finalAccount.getBalance().setScale(0)).isEqualTo(expectedBalance);
        
        // 거래 내역 순서 확인
        List<Transaction> transactions = transactionRepository.findByAccountOrderByIdDesc(accountA);
        assertThat(transactions).isSortedAccordingTo((t1, t2) -> t2.getId().compareTo(t1.getId()));
    }

    @Test
    @DisplayName("동시 출금 시 잔액 정확성 테스트")
    @Transactional
    void concurrentWithdrawalTest() throws InterruptedException {
        // given
        int threadCount = 10;
        int withdrawalCount = 100;
        BigDecimal withdrawalAmount = new BigDecimal("10000");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    for (int j = 0; j < withdrawalCount; j++) {
                        try {
                            transactionService.withdraw(accountA.getAccountNumber(), withdrawalAmount);
                            successCount.incrementAndGet();
                        } catch (BusinessException e) {
                            failCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Account finalAccount = accountRepository.findByAccountNumber(accountA.getAccountNumber()).orElseThrow();
        BigDecimal expectedBalance = new BigDecimal("10000000")
            .subtract(withdrawalAmount.multiply(new BigDecimal(successCount.get())))
            .setScale(0);
        
        assertThat(finalAccount.getBalance().setScale(0)).isEqualTo(expectedBalance);
        
        // 거래 내역 순서 확인
        List<Transaction> transactions = transactionRepository.findByAccountOrderByIdDesc(accountA);
        assertThat(transactions).isSortedAccordingTo((t1, t2) -> t2.getId().compareTo(t1.getId()));
    }

    @Test
    @DisplayName("동시 잔액 조회 시 성능 테스트")
    @Transactional
    void concurrentBalanceCheckTest() throws InterruptedException {
        // given
        int threadCount = 100;
        int checkCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<BigDecimal> balances = new ArrayList<>();
        
        // 계좌에 초기 입금
        transactionService.deposit(accountA.getAccountNumber(), new BigDecimal("1000000"));

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    for (int j = 0; j < checkCount; j++) {
                        Account account = accountRepository.findByAccountNumber(accountA.getAccountNumber()).orElseThrow();
                        synchronized (balances) {
                            balances.add(account.getBalance());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        BigDecimal expectedBalance = new BigDecimal("11000000").setScale(0);
        assertThat(balances).allMatch(balance -> balance.setScale(0).equals(expectedBalance));
    }
} 