package com.example.demo.transaction.entity;

import com.example.demo.account.entity.Account;
import com.example.demo.transaction.type.TransactionType;
import com.example.demo.common.BaseEntity;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Column(precision = 19, scale = 2)
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
} 