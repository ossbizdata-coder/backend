package com.oss.repository;
import com.oss.model.DailySummary;
import com.oss.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    /**
     * Find summary for a specific shop and date
     */
    Optional<DailySummary> findByShopAndBusinessDate(Shop shop, LocalDate businessDate);
    Optional<DailySummary> findByShop_IdAndBusinessDate(Long shopId, LocalDate businessDate);
    /**
     * Find all summaries for a specific date (all shops)
     */
    List<DailySummary> findByBusinessDate(LocalDate businessDate);
    /**
     * Find summaries for a date range
     */
    @Query("SELECT ds FROM DailySummary ds WHERE ds.businessDate BETWEEN :startDate AND :endDate ORDER BY ds.businessDate DESC")
    List<DailySummary> findByBusinessDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    /**
     * Find summaries for a shop within date range
     */
    @Query("SELECT ds FROM DailySummary ds WHERE ds.shop.id = :shopId AND ds.businessDate BETWEEN :startDate AND :endDate ORDER BY ds.businessDate DESC")
    List<DailySummary> findByShopIdAndBusinessDateBetween(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    /**
     * Find summaries for a shop within a specific year and month
     * Note: Callers should compute startDate and endDate from year/month
     */
    default List<DailySummary> findByShopIdAndYearMonth(Long shopId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return findByShopIdAndBusinessDateBetween(shopId, startDate, endDate);
    }
    /**
     * Find all summaries for a specific year and month
     * Note: Callers should compute startDate and endDate from year/month
     */
    default List<DailySummary> findByYearMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return findByBusinessDateBetween(startDate, endDate);
    }
    @Query("SELECT COALESCE(SUM(ds.totalRevenue), 0) FROM DailySummary ds WHERE ds.shop.id = :shopId AND ds.businessDate BETWEEN :startDate AND :endDate")
    Double sumRevenueByShopAndDateRange(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    @Query("SELECT COALESCE(SUM(ds.totalExpenses), 0) FROM DailySummary ds WHERE ds.shop.id = :shopId AND ds.businessDate BETWEEN :startDate AND :endDate")
    Double sumExpensesByShopAndDateRange(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    @Query("SELECT " +
           "COALESCE(SUM(ds.totalRevenue), 0) as totalRevenue, " +
           "COALESCE(SUM(ds.totalExpenses), 0) as totalExpenses, " +
           "COALESCE(SUM(ds.profit), 0) as totalProfit " +
           "FROM DailySummary ds WHERE ds.businessDate BETWEEN :startDate AND :endDate")
    Object[] sumTotalsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    /**
     * Find all closed summaries (for reporting)
     */
    @Query("SELECT ds FROM DailySummary ds WHERE ds.isClosed = true ORDER BY ds.businessDate DESC")
    List<DailySummary> findAllClosed();
    /**
     * Find latest summary for a shop
     */
    @Query("SELECT ds FROM DailySummary ds WHERE ds.shop.id = :shopId ORDER BY ds.businessDate DESC LIMIT 1")
    Optional<DailySummary> findLatestByShopId(@Param("shopId") Long shopId);
}