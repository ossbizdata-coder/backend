package com.oss.repository;

import com.oss.model.Credit;
import com.oss.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {
    List<Credit> findAllByOrderByCreatedAtDesc();

    List<Credit> findByIsPaidOrderByCreatedAtDesc(Boolean isPaid);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.isPaid = false")
    Double sumUnpaidCredits();

    @Query("SELECT COUNT(c) FROM Credit c WHERE c.isPaid = false")
    Long countUnpaidCredits();

    // Find credits by department and date (for sales calculation)
    @Query("SELECT c FROM Credit c WHERE c.department = :department AND c.transactionDate = :date")
    List<Credit> findByDepartmentAndTransactionDate(
            @Param("department") String department,
            @Param("date") LocalDate date
    );

    // Sum credits by department and date
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.department = :department AND c.transactionDate = :date")
    Double sumCreditsByDepartmentAndDate(
            @Param("department") String department,
            @Param("date") LocalDate date
    );

    // NEW: Find credits by shop and date
    @Query("SELECT c FROM Credit c WHERE c.shop = :shop AND c.transactionDate = :date")
    List<Credit> findByShopAndTransactionDate(
            @Param("shop") Shop shop,
            @Param("date") LocalDate date
    );

    // NEW: Sum credits by shop and date
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.shop = :shop AND c.transactionDate = :date")
    Double sumCreditsByShopAndDate(
            @Param("shop") Shop shop,
            @Param("date") LocalDate date
    );

    // Find credits by shop code (department) and date
    // Note: transaction_date is stored as LocalDate in database
    @Query("SELECT c FROM Credit c WHERE c.department = :shopCode " +
           "AND c.transactionDate = :date " +
           "ORDER BY c.createdAt DESC")
    List<Credit> findByShopCodeAndTransactionDate(
            @Param("shopCode") String shopCode,
            @Param("date") LocalDate date
    );

    // Sum credits by shop code (department) and date
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c " +
           "WHERE c.department = :shopCode " +
           "AND c.transactionDate = :date")
    Double sumCreditsByShopCodeAndDate(
            @Param("shopCode") String shopCode,
            @Param("date") LocalDate date
    );

    // NEW: Sum credits by date range (for monthly sales calculation)
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c " +
           "WHERE c.transactionDate >= :startDate " +
           "AND c.transactionDate <= :endDate")
    Double sumCreditsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // NEW: Sum credits by shop and date range (for monthly sales calculation per shop)
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c " +
           "WHERE c.shop.id = :shopId " +
           "AND c.transactionDate >= :startDate " +
           "AND c.transactionDate <= :endDate")
    Double sumCreditsByShopAndDateRange(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ==================== STAFF SALARY CALCULATION APIs ====================

    // Get all credits for a specific user (staff member)
    @Query("SELECT c FROM Credit c WHERE c.user.id = :userId ORDER BY c.transactionDate DESC, c.createdAt DESC")
    List<Credit> findByUserId(@Param("userId") Long userId);

    // Get credits for a user by paid status
    @Query("SELECT c FROM Credit c WHERE c.user.id = :userId AND c.isPaid = :isPaid ORDER BY c.transactionDate DESC, c.createdAt DESC")
    List<Credit> findByUserIdAndPaidStatus(
            @Param("userId") Long userId,
            @Param("isPaid") Boolean isPaid
    );

    // Get credits for a user within a date range (for daily/monthly salary)
    @Query("SELECT c FROM Credit c WHERE c.user.id = :userId " +
           "AND c.transactionDate >= :startDate " +
           "AND c.transactionDate <= :endDate " +
           "ORDER BY c.transactionDate DESC, c.createdAt DESC")
    List<Credit> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Sum total credits for a user (all time)
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.user.id = :userId")
    Double sumCreditsByUserId(@Param("userId") Long userId);

    // Sum unpaid credits for a user
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.user.id = :userId AND c.isPaid = false")
    Double sumUnpaidCreditsByUserId(@Param("userId") Long userId);

    // Sum paid credits for a user
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.user.id = :userId AND c.isPaid = true")
    Double sumPaidCreditsByUserId(@Param("userId") Long userId);

    // Sum credits for a user within a date range (for salary calculation)
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.user.id = :userId " +
           "AND c.transactionDate >= :startDate " +
           "AND c.transactionDate <= :endDate")
    Double sumCreditsByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Sum credits for a specific date (for daily salary calculation)
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.user.id = :userId AND c.transactionDate = :date")
    Double sumCreditsByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    // Get credits grouped by date for a user (useful for daily breakdown)
    @Query("SELECT c.transactionDate as date, COALESCE(SUM(c.amount), 0) as total " +
           "FROM Credit c WHERE c.user.id = :userId " +
           "AND c.transactionDate >= :startDate " +
           "AND c.transactionDate <= :endDate " +
           "GROUP BY c.transactionDate " +
           "ORDER BY c.transactionDate DESC")
    List<Object[]> sumCreditsByUserIdGroupedByDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
