package com.example.demo.transaction.repository;

import com.example.demo.account.entity.Account;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.type.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 거래 내역이 없어도 0을 반환
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account = :account AND t.type = :type " +
            "AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    BigDecimal getDailyTransactionAmount(
            @Param("account") Account account,
            @Param("type") TransactionType type,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 계좌의 거래 내역을 페이징하여 최신순으로 조회
    @Query(value = "SELECT t FROM Transaction t " +
            "JOIN FETCH t.account a " +
            "WHERE a.accountNumber = :accountNumber " +
            "ORDER BY t.id DESC",
            countQuery = "SELECT COUNT(t) FROM Transaction t " +
                    "JOIN t.account a " +
                    "WHERE a.accountNumber = :accountNumber")
    Page<Transaction> findByAccountNumberOrderByIdDesc(
            @Param("accountNumber") String accountNumber,
            Pageable pageable
    );

    List<Transaction> findByAccountOrderByIdDesc(Account account);
}