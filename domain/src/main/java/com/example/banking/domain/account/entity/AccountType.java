package com.example.banking.domain.account.entity;

import com.example.banking.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AccountType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal transferFeeRate;

    @Column(nullable = false, precision = 19)
    private BigDecimal dailyWithdrawalLimit;

    @Column(nullable = false, precision = 19)
    private BigDecimal dailyTransferLimit;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "accountType")
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();

    public void update(
        String description,
        BigDecimal transferFeeRate,
        BigDecimal dailyWithdrawalLimit,
        BigDecimal dailyTransferLimit
    ) {
        this.description = description;
        this.transferFeeRate = transferFeeRate;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
        this.dailyTransferLimit = dailyTransferLimit;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public BigDecimal calculateTransferFee(BigDecimal amount) {
        return amount.multiply(transferFeeRate).setScale(0, java.math.RoundingMode.DOWN);
    }
} 