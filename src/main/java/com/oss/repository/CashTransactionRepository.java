package com.oss.repository;

import com.oss.model.CashTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {

    List<CashTransaction> findByDailyCashId(Long dailyCashId);

    List<CashTransaction> findByDailyCashIdAndType(Long dailyCashId, String type);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CashTransaction ct WHERE ct.dailyCash.id = :dailyCashId AND ct.type = :type")
    Double sumAmountByDailyCashIdAndType(@Param("dailyCashId") Long dailyCashId, @Param("type") String type);

    @Query("SELECT ct FROM CashTransaction ct WHERE ct.dailyCash.id = :dailyCashId ORDER BY ct.createdAt DESC")
    List<CashTransaction> findByDailyCashIdOrderByCreatedAtDesc(@Param("dailyCashId") Long dailyCashId);

    // Expense Report Queries
    @Query("SELECT ct FROM CashTransaction ct WHERE ct.type = 'EXPENSE' AND ct.createdAt >= :startDate AND ct.createdAt < :endDate")
    List<CashTransaction> findExpensesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ct FROM CashTransaction ct WHERE ct.type = 'EXPENSE' AND ct.dailyCash.businessDate = :date")
    List<CashTransaction> findExpensesByDate(@Param("date") LocalDate date);

    // NOTE: Sales are NOT stored as transactions
    // Sales are calculated using: Sales = (Closing Cash - Opening Cash) + Expenses + Credits
    // The following queries are kept for historical data compatibility only

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CashTransaction ct WHERE ct.type = 'EXPENSE' AND ct.createdAt >= :startDate AND ct.createdAt < :endDate")
    Double sumExpensesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CashTransaction ct " +
           "WHERE ct.type = 'EXPENSE' " +
           "AND ct.dailyCash.shop.id = :shopId " +
           "AND ct.createdAt >= :startDate " +
           "AND ct.createdAt < :endDate")
    Double sumExpensesByShopAndDateRange(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

