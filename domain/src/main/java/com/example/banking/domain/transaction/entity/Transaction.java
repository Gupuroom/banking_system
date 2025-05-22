package com.example.banking.domain.transaction.entity;

import com.example.banking.core.common.BaseEntity;
import com.example.banking.domain.account.entity.Account;
import com.example.banking.domain.transaction.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq_gen")
    @SequenceGenerator(name = "transaction_seq_gen", sequenceName = "transaction_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false, precision = 19)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19)
    private BigDecimal balanceAfterTransaction;

    @Column(precision = 19)
    private BigDecimal fee;

    @Column(length = 20)
    private String relatedAccountNumber;

    public static Transaction createDeposit(Account account, BigDecimal amount) {
        return Transaction.builder()
                .account(account)
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .balanceAfterTransaction(account.getBalance())
                .fee(BigDecimal.ZERO)
                .build();
    }

    public static Transaction createWithdrawal(Account account, BigDecimal amount) {
        return Transaction.builder()
                .account(account)
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .balanceAfterTransaction(account.getBalance())
                .fee(BigDecimal.ZERO)
                .build();
    }

    public static Transaction createTransferOut(
        Account account,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal balanceAfterTransaction,
        String relatedAccountNumber
    ) {
        return Transaction.builder()
                .account(account)
                .type(TransactionType.TRANSFER_OUT)
                .amount(amount)
                .balanceAfterTransaction(balanceAfterTransaction)
                .fee(fee)
                .relatedAccountNumber(relatedAccountNumber)
                .build();
    }

    public static Transaction createTransferIn(
        Account account,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal balanceAfterTransaction,
        String relatedAccountNumber
    ) {
        return Transaction.builder()
                .account(account)
                .type(TransactionType.TRANSFER_IN)
                .amount(amount)
                .balanceAfterTransaction(balanceAfterTransaction)
                .fee(fee)
                .relatedAccountNumber(relatedAccountNumber)
                .build();
    }
} 