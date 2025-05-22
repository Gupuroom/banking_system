package com.example.banking.domain.account.repository;

import com.example.banking.domain.account.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {
}