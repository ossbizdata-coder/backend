package com.oss.repository;

import com.oss.model.DailyCash;
import com.oss.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCashRepository extends JpaRepository<DailyCash, Long> {

    Optional<DailyCash> findByShopAndBusinessDate(Shop shop, LocalDate businessDate);

    Optional<DailyCash> findByShop_IdAndBusinessDate(Long shopId, LocalDate businessDate);

    boolean existsByShopAndBusinessDate(Shop shop, LocalDate businessDate);

    boolean existsByShop_IdAndBusinessDate(Long shopId, LocalDate businessDate);

    @Query("SELECT dc FROM DailyCash dc WHERE dc.shop = :shop ORDER BY dc.businessDate DESC")
    List<DailyCash> findLatestByShop(@Param("shop") Shop shop);

    @Query("SELECT dc FROM DailyCash dc WHERE dc.shop.id = :shopId ORDER BY dc.businessDate DESC")
    List<DailyCash> findLatestByShopId(@Param("shopId") Long shopId);

    @Query("SELECT dc FROM DailyCash dc WHERE dc.shop = :shop AND dc.businessDate < :date ORDER BY dc.businessDate DESC")
    List<DailyCash> findPreviousByShopAndDate(@Param("shop") Shop shop, @Param("date") LocalDate date);

    /**
     * ✅ FIXED: Get the single most recent previous daily cash (prevents duplicates)
     * Uses native query with LIMIT 1 for guaranteed single result
     */
    @Query(value = "SELECT * FROM daily_cash " +
                   "WHERE shop_id = :shopId " +
                   "AND business_date < :date " +
                   "ORDER BY business_date DESC " +
                   "LIMIT 1", nativeQuery = true)
    DailyCash findLatestPreviousByShopIdAndDate(
        @Param("shopId") Long shopId,
        @Param("date") LocalDate date
    );

    /**
     * ✅ NEW: Find latest closed daily cash within a date range
     * This replaces multiple API calls with a single optimized query
     */
    @Query("SELECT dc FROM DailyCash dc " +
           "WHERE dc.shop.id = :shopId " +
           "AND dc.businessDate BETWEEN :startDate AND :endDate " +
           "AND dc.closingCash IS NOT NULL " +
           "AND dc.locked = true " +
           "ORDER BY dc.businessDate DESC")
    List<DailyCash> findLatestClosedByShopAndDateRange(
        @Param("shopId") Long shopId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * ✅ IMPROVED: Find previous daily cash before a given date by shopId
     */
    @Query("SELECT dc FROM DailyCash dc " +
           "WHERE dc.shop.id = :shopId " +
           "AND dc.businessDate < :date " +
           "ORDER BY dc.businessDate DESC")
    List<DailyCash> findPreviousByShopIdAndDate(
        @Param("shopId") Long shopId,
        @Param("date") LocalDate date
    );

    List<DailyCash> findByShopOrderByBusinessDateDesc(Shop shop);

    List<DailyCash> findByShop_IdOrderByBusinessDateDesc(Long shopId);
}

