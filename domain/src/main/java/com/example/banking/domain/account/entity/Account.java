package com.example.banking.domain.account.entity;

import com.example.banking.core.common.BaseEntity;
import com.example.banking.core.error.BusinessException;
import com.example.banking.domain.account.error.AccountErrorCode;
import com.example.banking.domain.account.type.AccountStatus;
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

    // 원화 기준이므로 소수점은 없앤다.
    @Column(nullable = false, precision = 19)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_type_id", nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    public static Account create(String accountNumber, BigDecimal initialBalance, AccountType accountType) {
        return Account.builder()
                .accountNumber(accountNumber)
                .balance(initialBalance)
                .accountType(accountType)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException(AccountErrorCode.INSUFFICIENT_BALANCE);
        }
        this.balance = this.balance.subtract(amount);
    }

    public void delete() {
        if (status == AccountStatus.DELETED) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_ALREADY_DELETED);
        }
        this.status = AccountStatus.DELETED;
    }

    public BigDecimal calculateTransferFee(BigDecimal amount) {
        return this.accountType.calculateTransferFee(amount);
    }

    public BigDecimal getDailyWithdrawalLimit() {
        return this.accountType.getDailyWithdrawalLimit();
    }

    public BigDecimal getDailyTransferLimit() {
        return this.accountType.getDailyTransferLimit();
    }
}