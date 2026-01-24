package com.oss.service;

import com.oss.dto.*;
import com.oss.model.*;
import com.oss.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final CashTransactionRepository cashTransactionRepository;
    private final DailyCashRepository dailyCashRepository;
    private final ShopRepository shopRepository;
    private final DailySummaryRepository dailySummaryRepository;

    public ReportService(
            CashTransactionRepository cashTransactionRepository,
            DailyCashRepository dailyCashRepository,
            ShopRepository shopRepository,
            DailySummaryRepository dailySummaryRepository) {
        this.cashTransactionRepository = cashTransactionRepository;
        this.dailyCashRepository = dailyCashRepository;
        this.shopRepository = shopRepository;
        this.dailySummaryRepository = dailySummaryRepository;
    }

    public List<BankDepositDTO> getBankDeposits(LocalDate startDate, LocalDate endDate) {
        List<DailyCash> dailyCashRecords = dailyCashRepository.findByBusinessDateBetween(startDate, endDate);

        return dailyCashRecords.stream()
                .filter(dc -> dc.getLocked() && dc.getClosingCash() != null)
                .map(dc -> BankDepositDTO.builder()
                        .shopName(dc.getShop().getName())
                        .amount(dc.getClosingCash())
                        .build())
                .collect(Collectors.toList());
    }

    public DailyExpenseReportDTO getDailyExpenseReport(LocalDate date) {
        // Use findByBusinessDateBetween with same date for both params
        List<DailyCash> dailyCashList = dailyCashRepository.findByBusinessDateBetween(date, date);

        List<ShopExpenseDTO> shopExpenses = new ArrayList<>();
        double totalExpenses = 0.0;

        for (DailyCash dc : dailyCashList) {
            List<CashTransaction> expenses = cashTransactionRepository.findByDailyCashIdAndType(dc.getId(), "EXPENSE");
            double shopExpenseTotal = expenses.stream().mapToDouble(CashTransaction::getAmount).sum();
            totalExpenses += shopExpenseTotal;

            shopExpenses.add(ShopExpenseDTO.builder()
                    .shopName(dc.getShop().getName())
                    .totalExpenses(shopExpenseTotal)
                    .build());
        }

        return DailyExpenseReportDTO.builder()
                .date(date.toString())
                .totalExpenses(totalExpenses)
                .shopExpenses(shopExpenses)
                .build();
    }

    public MonthlyExpenseReportDTO getMonthlyExpenseReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<DailyCash> dailyCashList = dailyCashRepository.findByBusinessDateBetween(startDate, endDate);

        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpenses = 0.0;

        for (DailyCash dc : dailyCashList) {
            List<CashTransaction> expenses = cashTransactionRepository.findByDailyCashIdAndType(dc.getId(), "EXPENSE");

            for (CashTransaction exp : expenses) {
                totalExpenses += exp.getAmount();
                String category = exp.getExpenseType() != null ? exp.getExpenseType().getName() : "Other";
                categoryTotals.merge(category, exp.getAmount(), Double::sum);
            }
        }

        List<CategoryExpenseDTO> categories = categoryTotals.entrySet().stream()
                .map(e -> CategoryExpenseDTO.builder()
                        .expenseTypeName(e.getKey())
                        .totalAmount(e.getValue())
                        .build())
                .sorted((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()))
                .collect(Collectors.toList());

        return MonthlyExpenseReportDTO.builder()
                .year(year)
                .month(month)
                .totalExpenses(totalExpenses)
                .categoryExpenses(categories)
                .build();
    }

    public ExpenseByCategoryReportDTO getExpensesByCategory(LocalDate startDate, LocalDate endDate) {
        List<DailyCash> dailyCashList = dailyCashRepository.findByBusinessDateBetween(startDate, endDate);

        Map<String, Double> categoryTotals = new HashMap<>();

        for (DailyCash dc : dailyCashList) {
            List<CashTransaction> expenses = cashTransactionRepository.findByDailyCashIdAndType(dc.getId(), "EXPENSE");

            for (CashTransaction exp : expenses) {
                String category = exp.getExpenseType() != null ? exp.getExpenseType().getName() : "Other";
                categoryTotals.merge(category, exp.getAmount(), Double::sum);
            }
        }

        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();

        List<CategoryExpenseDTO> categories = categoryTotals.entrySet().stream()
                .map(e -> CategoryExpenseDTO.builder()
                        .expenseTypeName(e.getKey())
                        .totalAmount(e.getValue())
                        .build())
                .sorted((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()))
                .collect(Collectors.toList());

        return ExpenseByCategoryReportDTO.builder()
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .totalExpenses(total)
                .categories(categories)
                .build();
    }

    public ExpenseByShopReportDTO getExpensesByShop(LocalDate startDate, LocalDate endDate) {
        List<DailyCash> dailyCashList = dailyCashRepository.findByBusinessDateBetween(startDate, endDate);

        Map<String, ShopExpenseDetailDTO> shopExpenses = new HashMap<>();

        for (DailyCash dc : dailyCashList) {
            List<CashTransaction> expenses = cashTransactionRepository.findByDailyCashIdAndType(dc.getId(), "EXPENSE");
            double totalExpense = expenses.stream().mapToDouble(CashTransaction::getAmount).sum();

            shopExpenses.merge(dc.getShop().getName(),
                ShopExpenseDetailDTO.builder()
                    .shopName(dc.getShop().getName())
                    .totalExpenses(totalExpense)
                    .build(),
                (existing, newVal) -> ShopExpenseDetailDTO.builder()
                    .shopName(existing.getShopName())
                    .totalExpenses(existing.getTotalExpenses() + newVal.getTotalExpenses())
                    .build());
        }

        List<ShopExpenseDetailDTO> shops = new ArrayList<>(shopExpenses.values());
        shops.sort((a, b) -> Double.compare(b.getTotalExpenses(), a.getTotalExpenses()));

        return ExpenseByShopReportDTO.builder()
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .shops(shops)
                .build();
    }

    public BusinessSummaryDTO getBusinessSummary() {
        // Return a simple placeholder - the actual DTO expects complex nested objects
        return BusinessSummaryDTO.builder()
                .build();
    }

    public Map<String, Object> getStaffPerformance(LocalDate startDate, LocalDate endDate) {
        List<DailySummary> summaries = dailySummaryRepository.findByBusinessDateBetween(startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("data", summaries);

        return result;
    }

    public Map<String, Object> getShopPerformance(Long shopId, LocalDate startDate, LocalDate endDate) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Shop not found"));

        List<DailySummary> summaries = dailySummaryRepository.findByShopIdAndBusinessDateBetween(shopId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("shopName", shop.getName());
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("data", summaries);

        return result;
    }
}
