package com.oss.service;
import com.oss.model.*;
import com.oss.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Service to migrate data from old shop_transactions to new cash management system
 * This is a ONE-TIME migration utility
 */
@Service
public class DataMigrationService {
    private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);
    private final ShopRepository shopRepo;
    private final DailyCashRepository dailyCashRepo;
    private final CashTransactionRepository cashTransactionRepo;
    private final TransactionRepository oldTransactionRepo;
    private final CreditRepository creditRepo;
    public DataMigrationService(
            ShopRepository shopRepo,
            DailyCashRepository dailyCashRepo,
            CashTransactionRepository cashTransactionRepo,
            TransactionRepository oldTransactionRepo,
            CreditRepository creditRepo) {
        this.shopRepo = shopRepo;
        this.dailyCashRepo = dailyCashRepo;
        this.cashTransactionRepo = cashTransactionRepo;
        this.oldTransactionRepo = oldTransactionRepo;
        this.creditRepo = creditRepo;
    }
    /**
     * Initialize shops if they don't exist
     */
    @Transactional
    public void initializeShops() {
        if (shopRepo.findByCode("CAFE").isEmpty()) {
            Shop cafe = Shop.builder()
                    .code("CAFE")
                    .name("Cafe Shop")
                    .build();
            shopRepo.save(cafe);
        }
        if (shopRepo.findByCode("BOOKSHOP").isEmpty()) {
            Shop bookshop = Shop.builder()
                    .code("BOOKSHOP")
                    .name("Book Shop")
                    .build();
            shopRepo.save(bookshop);
        }
    }
    /**
     * Migrate old shop_transactions to new structure
     * WARNING: This should only be run ONCE
     */
    @Transactional
    public void migrateShopTransactions() {
        // Get all old transactions grouped by shop and date
        List<Transaction> allOldTransactions = oldTransactionRepo.findAll();
        // Group by shopType and businessDate
        Map<String, Map<LocalDate, List<Transaction>>> groupedTransactions = allOldTransactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getShopType() != null ? t.getShopType() : t.getDepartment(),
                        Collectors.groupingBy(t -> {
                            if (t.getBusinessDate() != null) {
                                return Instant.ofEpochMilli(t.getBusinessDate())
                                        .atZone(ZoneId.of("UTC"))
                                        .toLocalDate();
                            }
                            return LocalDate.now();
                        })
                ));
        groupedTransactions.forEach((shopCode, dateMap) -> {
            Shop shop = shopRepo.findByCode(shopCode).orElse(null);
            if (shop == null) {
                return;
            }
            dateMap.forEach((date, transactions) -> {
                migrateDailyTransactions(shop, date, transactions);
            });
        });
    }
    /**
     * Migrate transactions for a specific shop and date
     */
    private void migrateDailyTransactions(Shop shop, LocalDate date, List<Transaction> transactions) {
        // Check if daily_cash already exists
        if (dailyCashRepo.existsByShopAndBusinessDate(shop, date)) {
            return;
        }
        // Find opening/closing balance from SALE transactions
        Transaction saleTransaction = transactions.stream()
                .filter(t -> "SALE".equals(t.getCategory()))
                .filter(t -> t.getOpeningBalance() != null && t.getClosingBalance() != null)
                .findFirst()
                .orElse(null);
        Double openingCash = saleTransaction != null ? saleTransaction.getOpeningBalance() : 0.0;
        Double closingCash = saleTransaction != null ? saleTransaction.getClosingBalance() : null;
        User closedBy = saleTransaction != null ? saleTransaction.getUser() : null;
        // Create DailyCash record
        DailyCash dailyCash = DailyCash.builder()
                .shop(shop)
                .businessDate(date)
                .openingCash(openingCash)
                .closingCash(closingCash)
                .locked(closingCash != null)
                .closedBy(closedBy)
                .build();
        dailyCash = dailyCashRepo.save(dailyCash);
        // Migrate expense transactions
        DailyCash finalDailyCash = dailyCash;
        List<Transaction> expenses = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getCategory()))
                .collect(Collectors.toList());
        for (Transaction expense : expenses) {
            CashTransaction cashTransaction = CashTransaction.builder()
                    .dailyCash(finalDailyCash)
                    .type("EXPENSE")
                    .amount(expense.getAmount())
                    .expenseType(expense.getExpenseType())
                    .description(expense.getComment() != null ? expense.getComment() : expense.getItemName())
                    .recordedBy(expense.getUser())
                    .createdAt(expense.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();
            cashTransactionRepo.save(cashTransaction);
        }
    }
    /**
     * Update existing credits to link to shops based on department
     */
    @Transactional
    public void updateCreditsWithShops() {
        List<Credit> allCredits = creditRepo.findAll();
        Shop cafe = shopRepo.findByCode("CAFE").orElse(null);
        Shop bookshop = shopRepo.findByCode("BOOKSHOP").orElse(null);
        int updated = 0;
        for (Credit credit : allCredits) {
            if (credit.getShop() == null && credit.getDepartment() != null) {
                if ("CAFE".equalsIgnoreCase(credit.getDepartment()) && cafe != null) {
                    credit.setShop(cafe);
                    creditRepo.save(credit);
                    updated++;
                } else if ("BOOKSHOP".equalsIgnoreCase(credit.getDepartment()) && bookshop != null) {
                    credit.setShop(bookshop);
                    creditRepo.save(credit);
                    updated++;
                }
            }
        }
    }
    /**
     * Run complete migration
     */
    @Transactional
    public void runFullMigration() {
        initializeShops();
        migrateShopTransactions();
        updateCreditsWithShops();
    }
}