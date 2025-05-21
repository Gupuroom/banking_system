package com.example.demo.account.repository;

import com.example.demo.account.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {
}