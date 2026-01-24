package com.oss.service;
import com.oss.model.*;
import com.oss.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Daily Summary Service - Automatically calculates and caches daily metrics
 * This dramatically improves performance for reports and analytics
 */
@Service
public class DailySummaryService {
    private final DailySummaryRepository dailySummaryRepo;
    private final DailyCashRepository dailyCashRepo;
    private final CashTransactionRepository cashTransactionRepo;
    private final CreditRepository creditRepo;
    private final AttendanceRepository attendanceRepo;
    public DailySummaryService(DailySummaryRepository dailySummaryRepo,
                               DailyCashRepository dailyCashRepo,
                               CashTransactionRepository cashTransactionRepo,
                               CreditRepository creditRepo,
                               AttendanceRepository attendanceRepo) {
        this.dailySummaryRepo = dailySummaryRepo;
        this.dailyCashRepo = dailyCashRepo;
        this.cashTransactionRepo = cashTransactionRepo;
        this.creditRepo = creditRepo;
        this.attendanceRepo = attendanceRepo;
    }
    /**
     * Calculate and save daily summary when a day is closed
     * This is called automatically from DailyCashService.closeDay()
     */
    @Transactional
    public DailySummary calculateAndSaveDailySummary(DailyCash dailyCash) {
        Shop shop = dailyCash.getShop();
        LocalDate businessDate = dailyCash.getBusinessDate();
        // Check if summary already exists
        DailySummary existing = dailySummaryRepo.findByShopAndBusinessDate(shop, businessDate).orElse(null);
        if (existing != null) {
            // Update existing summary
            return updateDailySummary(existing, dailyCash);
        }
        // Calculate all metrics
        Double openingCash = dailyCash.getOpeningCash();
        Double closingCash = dailyCash.getClosingCash();
        Double cashDifference = closingCash != null ? closingCash - openingCash : 0.0;
        // Get expenses for this day
        List<CashTransaction> expenses = cashTransactionRepo.findExpensesByDate(businessDate);
        List<CashTransaction> shopExpenses = expenses.stream()
                .filter(ct -> ct.getDailyCash().getShop().getId().equals(shop.getId()))
                .collect(Collectors.toList());
        Double totalExpenses = shopExpenses.stream()
                .mapToDouble(CashTransaction::getAmount)
                .sum();
        // Get credits for this day using shop parameter
        List<Credit> credits = creditRepo.findByShopAndTransactionDate(shop, businessDate);
        Double totalCredits = credits.stream()
                .mapToDouble(Credit::getAmount)
                .sum();
        // Calculate revenue: (Closing - Opening) + Expenses
        Double totalRevenue = cashDifference + totalExpenses;
        // Calculate net sales: Revenue - Credits
        Double netSales = totalRevenue - totalCredits;
        // Calculate profit: Revenue - Expenses
        Double profit = totalRevenue - totalExpenses;
        // Get staff metrics - find attendances by business date for this shop
        // We'll match based on the daily cash shop
        List<Attendance> allAttendances = attendanceRepo.findAll();
        List<Attendance> attendances = allAttendances.stream()
                .filter(a -> {
                    LocalDate attendanceDate = LocalDate.ofInstant(a.getWorkDate(), java.time.ZoneId.systemDefault());
                    // For now, include all attendances on this date
                    // TODO: Add shop reference to User or Attendance for better filtering
                    return attendanceDate.equals(businessDate);
                })
                .collect(Collectors.toList());
        Integer staffCount = attendances.size();
        Double totalAttendanceHours = attendances.stream()
                .filter(a -> a.getCheckOutTime() != null && a.getCheckInTime() != null)
                .mapToDouble(a -> {
                    long durationMinutes = java.time.Duration.between(a.getCheckInTime(), a.getCheckOutTime()).toMinutes();
                    return durationMinutes / 60.0; // Convert to hours
                })
                .sum();
        // Get manual sales count - DailyCash doesn't have manualSales collection, default to 0
        int manualSaleCount = 0;
        // Build and save summary
        DailySummary summary = DailySummary.builder()
                .shop(shop)
                .businessDate(businessDate)
                .openingCash(openingCash)
                .closingCash(closingCash)
                .cashDifference(cashDifference)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .totalCredits(totalCredits)
                .netSales(netSales)
                .profit(profit)
                .expenseCount(shopExpenses.size())
                .creditCount(credits.size())
                .manualSaleCount(manualSaleCount)
                .staffCount(staffCount)
                .totalAttendanceHours(totalAttendanceHours)
                .isClosed(dailyCash.getLocked())
                .closedById(dailyCash.getClosedBy() != null ? dailyCash.getClosedBy().getId() : null)
                .closedByUser(dailyCash.getClosedBy())
                .closedAt(dailyCash.getClosedAt() != null ?
                    dailyCash.getClosedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .calculatedAt(System.currentTimeMillis())
                .build();
        return dailySummaryRepo.save(summary);
    }
    /**
     * Update existing daily summary
     */
    @Transactional
    public DailySummary updateDailySummary(DailySummary existing, DailyCash dailyCash) {
        Shop shop = dailyCash.getShop();
        LocalDate businessDate = dailyCash.getBusinessDate();
        // Recalculate all metrics
        Double openingCash = dailyCash.getOpeningCash();
        Double closingCash = dailyCash.getClosingCash();
        Double cashDifference = closingCash != null ? closingCash - openingCash : 0.0;
        List<CashTransaction> expenses = cashTransactionRepo.findExpensesByDate(businessDate);
        List<CashTransaction> shopExpenses = expenses.stream()
                .filter(ct -> ct.getDailyCash().getShop().getId().equals(shop.getId()))
                .collect(Collectors.toList());
        Double totalExpenses = shopExpenses.stream()
                .mapToDouble(CashTransaction::getAmount)
                .sum();
        List<Credit> credits = creditRepo.findByShopAndTransactionDate(shop, businessDate);
        Double totalCredits = credits.stream()
                .mapToDouble(Credit::getAmount)
                .sum();
        Double totalRevenue = cashDifference + totalExpenses;
        Double netSales = totalRevenue - totalCredits;
        Double profit = totalRevenue - totalExpenses;
        List<Attendance> allAttendances = attendanceRepo.findAll();
        List<Attendance> attendances = allAttendances.stream()
                .filter(a -> {
                    LocalDate attendanceDate = LocalDate.ofInstant(a.getWorkDate(), java.time.ZoneId.systemDefault());
                    return attendanceDate.equals(businessDate);
                })
                .collect(Collectors.toList());
        Integer staffCount = attendances.size();
        Double totalAttendanceHours = attendances.stream()
                .filter(a -> a.getCheckOutTime() != null && a.getCheckInTime() != null)
                .mapToDouble(a -> {
                    long durationMinutes = java.time.Duration.between(a.getCheckInTime(), a.getCheckOutTime()).toMinutes();
                    return durationMinutes / 60.0;
                })
                .sum();
        int manualSaleCount = 0;
        // Update existing summary
        existing.setOpeningCash(openingCash);
        existing.setClosingCash(closingCash);
        existing.setCashDifference(cashDifference);
        existing.setTotalRevenue(totalRevenue);
        existing.setTotalExpenses(totalExpenses);
        existing.setTotalCredits(totalCredits);
        existing.setNetSales(netSales);
        existing.setProfit(profit);
        existing.setExpenseCount(shopExpenses.size());
        existing.setCreditCount(credits.size());
        existing.setManualSaleCount(manualSaleCount);
        existing.setStaffCount(staffCount);
        existing.setTotalAttendanceHours(totalAttendanceHours);
        existing.setIsClosed(dailyCash.getLocked());
        existing.setClosedById(dailyCash.getClosedBy() != null ? dailyCash.getClosedBy().getId() : null);
        existing.setClosedByUser(dailyCash.getClosedBy());
        existing.setClosedAt(dailyCash.getClosedAt() != null ?
            dailyCash.getClosedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
        existing.setCalculatedAt(System.currentTimeMillis());
        return dailySummaryRepo.save(existing);
    }
    /**
     * Recalculate summaries for a date range (for data migration or corrections)
     */
    @Transactional
    public void recalculateSummaries(LocalDate startDate, LocalDate endDate) {
        List<DailyCash> dailyCashList = dailyCashRepo.findByBusinessDateBetween(startDate, endDate);
        for (DailyCash dailyCash : dailyCashList) {
            if (dailyCash.getLocked()) {
                calculateAndSaveDailySummary(dailyCash);
            }
        }
    }
    public DailySummary getSummary(Long shopId, LocalDate businessDate) {
        return dailySummaryRepo.findByShop_IdAndBusinessDate(shopId, businessDate).orElse(null);
    }
    public List<DailySummary> getSummaries(LocalDate startDate, LocalDate endDate) {
        return dailySummaryRepo.findByBusinessDateBetween(startDate, endDate);
    }
    public List<DailySummary> getSummariesByShop(Long shopId, LocalDate startDate, LocalDate endDate) {
        return dailySummaryRepo.findByShopIdAndBusinessDateBetween(shopId, startDate, endDate);
    }
}