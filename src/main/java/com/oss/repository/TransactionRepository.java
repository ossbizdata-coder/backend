package com.oss.repository;
import com.oss.model.Transaction;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.shopType = :shopType
          AND t.category = :category
          AND t.transactionTime >= :startTime
          AND t.transactionTime < :endTime
    """)
    Double sumByTypeAndTimeRange(
            String shopType,
            String category,
            Instant startTime,
            Instant endTime
                                );
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE t.transactionTime >= :start AND t.transactionTime < :end
        ORDER BY t.transactionTime DESC
    """)
    List<Transaction> findByTransactionTimeBetween(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
    // Filter by department and/or category using time range
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE (:department IS NULL OR t.department = :department)
          AND (:category IS NULL OR t.category = :category)
          AND t.transactionTime >= :startTime
          AND t.transactionTime < :endTime
        ORDER BY t.transactionTime DESC
    """)
    List<Transaction> findByDepartmentAndCategoryAndTimeRange(
            @Param("department") String department,
            @Param("category") String category,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
    // Sum expenses by department and date
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.department = :department
          AND t.category = 'EXPENSE'
          AND t.businessDate = :dateMillis
    """)
    Double sumExpensesByDepartmentAndDate(
            @Param("department") String department,
            @Param("dateMillis") Long dateMillis
    );
    // Sum sales by department and date range
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE (:department IS NULL OR t.department = :department)
          AND t.category = 'SALE'
          AND t.businessDate >= :startDateMillis
          AND t.businessDate <= :endDateMillis
    """)
    Double sumSalesByDepartmentAndDateRange(
            @Param("department") String department,
            @Param("startDateMillis") Long startDateMillis,
            @Param("endDateMillis") Long endDateMillis
    );
    // Sum expenses by department and date range
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE (:department IS NULL OR t.department = :department)
          AND t.category = 'EXPENSE'
          AND t.businessDate >= :startDateMillis
          AND t.businessDate <= :endDateMillis
    """)
    Double sumExpensesByDepartmentAndDateRange(
            @Param("department") String department,
            @Param("startDateMillis") Long startDateMillis,
            @Param("endDateMillis") Long endDateMillis
    );
    // Get transactions by department and category
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE t.department = :department AND t.category = :category
        ORDER BY t.transactionTime DESC
    """)
    List<Transaction> findByDepartmentAndCategoryOrderByTransactionTimeDesc(
            @Param("department") String department,
            @Param("category") String category
    );
    // Get all transactions by category
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE t.category = :category
        ORDER BY t.transactionTime DESC
    """)
    List<Transaction> findByCategoryOrderByTransactionTimeDesc(@Param("category") String category);
    // Get all transactions by department and date (using business_date timestamp)
    @Query(value = """
        SELECT t.* FROM shop_transactions t
        WHERE t.department = :department
          AND t.business_date = :dateMillis
        ORDER BY t.transaction_time ASC
    """, nativeQuery = true)
    List<Transaction> findByDepartmentAndBusinessDateNative(
            @Param("department") String department,
            @Param("dateMillis") Long dateMillis
    );
    // Get all transactions by business date (no filters, using timestamp)
    @Query(value = """
        SELECT t.* FROM shop_transactions t
        WHERE t.business_date = :dateMillis
        ORDER BY t.transaction_time ASC
    """, nativeQuery = true)
    List<Transaction> findByBusinessDateNative(@Param("dateMillis") Long dateMillis);
    // Keep JPA versions for backward compatibility
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE t.department = :department
          AND t.businessDate = :dateMillis
        ORDER BY t.transactionTime ASC
    """)
    List<Transaction> findByDepartmentAndBusinessDate(
            @Param("department") String department,
            @Param("dateMillis") Long dateMillis
    );
    // Get all transactions by business date (no filters)
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE t.businessDate = :dateMillis
        ORDER BY t.transactionTime ASC
    """)
    List<Transaction> findByBusinessDate(@Param("dateMillis") Long dateMillis);
    // Get all transactions within a date range
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE t.businessDate >= :startDateMillis
          AND t.businessDate <= :endDateMillis
        ORDER BY t.businessDate DESC, t.transactionTime DESC
    """)
    List<Transaction> findByBusinessDateBetween(
            @Param("startDateMillis") Long startDateMillis,
            @Param("endDateMillis") Long endDateMillis
    );
    // Get the latest closing balance for a department
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.department = :department
          AND t.category = 'SALE'
          AND t.closingBalance IS NOT NULL
          AND t.closingBalance > 0
        ORDER BY t.transactionTime DESC
        LIMIT 1
    """)
    Transaction findLatestClosingBalanceByDepartment(@Param("department") String department);
    // Get transactions by department, category and date
    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.expenseType
        WHERE (:department IS NULL OR t.department = :department)
          AND (:category IS NULL OR t.category = :category)
          AND t.businessDate = :dateMillis
        ORDER BY t.transactionTime DESC
    """)
    List<Transaction> findByDepartmentAndCategoryAndBusinessDate(
            @Param("department") String department,
            @Param("category") String category,
            @Param("dateMillis") Long dateMillis
    );
}