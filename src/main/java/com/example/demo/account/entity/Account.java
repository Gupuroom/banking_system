package com.example.demo.account.entity;

import com.example.demo.account.error.AccountErrorCode;
import com.example.demo.account.type.AccountStatus;
import com.example.demo.common.BaseEntity;
import com.example.demo.common.error.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    public static Account create(String accountNumber, BigDecimal initialBalance) {
        return Account.builder()
                .accountNumber(accountNumber)
                .balance(initialBalance)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    public void delete() {
        if (status == AccountStatus.DELETED) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        status = AccountStatus.DELETED;
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}