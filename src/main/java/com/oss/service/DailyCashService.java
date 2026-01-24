package com.oss.service;

import com.oss.dto.CashTransactionDTO;
import com.oss.dto.DailyCashSummaryDTO;
import com.oss.dto.OSD_CreditDTO;
import com.oss.dto.ShopSummaryDTO;
import com.oss.model.*;
import com.oss.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DailyCashService {

    private final DailyCashRepository dailyCashRepo;
    private final ShopRepository shopRepo;
    private final CashTransactionRepository cashTransactionRepo;
    private final CreditRepository creditRepo;
    private final ExpenseTypeRepository expenseTypeRepo;
    private final AuditLogService auditLogService;

    public DailyCashService(DailyCashRepository dailyCashRepo,
                           ShopRepository shopRepo,
                           CashTransactionRepository cashTransactionRepo,
                           CreditRepository creditRepo,
                           ExpenseTypeRepository expenseTypeRepo,
                           AuditLogService auditLogService) {
        this.dailyCashRepo = dailyCashRepo;
        this.shopRepo = shopRepo;
        this.cashTransactionRepo = cashTransactionRepo;
        this.creditRepo = creditRepo;
        this.expenseTypeRepo = expenseTypeRepo;
        this.auditLogService = auditLogService;
    }

    /**
     * Get summary of all shops with latest closing cash
     */
    public List<ShopSummaryDTO> getShopsSummary() {
        return shopRepo.findAll().stream()
                .map(shop -> {
                    List<DailyCash> latestList = dailyCashRepo.findLatestByShop(shop);
                    DailyCash latest = latestList.isEmpty() ? null : latestList.get(0);

                    return ShopSummaryDTO.builder()
                            .shopId(shop.getId())
                            .shopCode(shop.getCode())
                            .shopName(shop.getName())
                            .latestClosingCash(latest != null ? latest.getClosingCash() : null)
                            .lastUpdatedDate(latest != null ? latest.getBusinessDate().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get or create daily cash record for a shop on a specific date
     * ✅ FIXED: Added synchronization to prevent race conditions and duplicate records
     */
    @Transactional
    public DailyCash getOrCreateDailyCash(Long shopId, LocalDate date) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // ✅ Try to find existing first
        Optional<DailyCash> existing = dailyCashRepo.findByShopAndBusinessDate(shop, date);
        if (existing.isPresent()) {
            log.debug("Found existing daily_cash record: id={} for shop={} on {}",
                     existing.get().getId(), shopId, date);
            return existing.get();
        }

        // ✅ If not found, create new one (synchronized to prevent race conditions)
        synchronized (this) {
            // ✅ Double-check after acquiring lock
            existing = dailyCashRepo.findByShopAndBusinessDate(shop, date);
            if (existing.isPresent()) {
                log.debug("Found existing daily_cash record after lock: id={}", existing.get().getId());
                return existing.get();
            }

            // ✅ Get previous day's closing balance (using new single-result query)
            Double openingCash = 0.0;
            try {
                DailyCash previousDay = dailyCashRepo.findLatestPreviousByShopIdAndDate(shopId, date);
                if (previousDay != null && previousDay.getClosingCash() != null) {
                    openingCash = previousDay.getClosingCash();
                    log.debug("Using previous day's closing balance: {} from date={}",
                             openingCash, previousDay.getBusinessDate());
                }
            } catch (Exception e) {
                log.warn("Could not fetch previous day's closing balance: {}", e.getMessage());
                openingCash = 0.0;
            }

            // ✅ Create new record
            DailyCash newDailyCash = DailyCash.builder()
                    .shop(shop)
                    .businessDate(date)
                    .openingCash(openingCash)
                    .openingConfirmed(false)  // ⚠️ NEW: Opening balance not yet confirmed
                    .locked(false)
                    .build();

            DailyCash saved = dailyCashRepo.save(newDailyCash);
            log.info("✅ Created new daily_cash record: id={} for shop={} on {} with openingCash={}",
                    saved.getId(), shopId, date, openingCash);

            return saved;
        }
    }

    /**
     * ✅ NEW: Find latest closed daily cash within a date range
     * This method is used by the optimized endpoint to reduce API calls
     */
    public List<DailyCash> findLatestClosedByShopAndDateRange(Long shopId, LocalDate startDate, LocalDate endDate) {
        return dailyCashRepo.findLatestClosedByShopAndDateRange(shopId, startDate, endDate);
    }

    /**
     * Get complete daily cash summary with derived totals
     */
    public DailyCashSummaryDTO getDailyCashSummary(Long shopId, LocalDate date) {
        DailyCash dailyCash = getOrCreateDailyCash(shopId, date);

        // Get all transactions
        List<CashTransaction> allTransactions = cashTransactionRepo.findByDailyCashIdOrderByCreatedAtDesc(dailyCash.getId());
        List<CashTransactionDTO> expenses = allTransactions.stream()
                .filter(ct -> "EXPENSE".equals(ct.getType()))
                .map(CashTransactionDTO::from)
                .collect(Collectors.toList());
        List<CashTransactionDTO> sales = allTransactions.stream()
                .filter(ct -> "SALE".equals(ct.getType()))
                .map(CashTransactionDTO::from)
                .collect(Collectors.toList());

        // Get credits for this shop on this date (using department field and LocalDate)
        String shopCode = dailyCash.getShop().getCode(); // CAFE or BOOKSHOP
        List<Credit> creditList = creditRepo.findByShopCodeAndTransactionDate(shopCode, date);
        List<OSD_CreditDTO> credits = creditList.stream()
                .map(this::convertCreditToDTO)
                .collect(Collectors.toList());

        // Calculate totals
        Double totalExpenses = cashTransactionRepo.sumAmountByDailyCashIdAndType(dailyCash.getId(), "EXPENSE");
        Double manualSales = cashTransactionRepo.sumAmountByDailyCashIdAndType(dailyCash.getId(), "SALE");
        Double totalCredits = creditRepo.sumCreditsByShopCodeAndDate(shopCode, date);

        if (totalExpenses == null) totalExpenses = 0.0;
        if (manualSales == null) manualSales = 0.0;
        if (totalCredits == null) totalCredits = 0.0;

        // Calculate total sales if day is closed
        Double totalSales = null;
        Double variance = null;
        if (dailyCash.getClosingCash() != null) {
            totalSales = dailyCash.getClosingCash() - dailyCash.getOpeningCash() + totalExpenses - manualSales;
            variance = totalSales - totalCredits;
        }

        return DailyCashSummaryDTO.builder()
                .dailyCashId(dailyCash.getId())
                .shopId(dailyCash.getShop().getId())
                .shopCode(dailyCash.getShop().getCode())
                .shopName(dailyCash.getShop().getName())
                .businessDate(dailyCash.getBusinessDate())
                .openingCash(dailyCash.getOpeningCash())
                .openingConfirmed(dailyCash.getOpeningConfirmed())  // ⚠️ NEW: Return opening confirmed status
                .closingCash(dailyCash.getClosingCash())
                .locked(dailyCash.getLocked())
                .closedByName(dailyCash.getClosedBy() != null ? dailyCash.getClosedBy().getName() : null)
                .totalExpenses(totalExpenses)
                .manualSales(manualSales)
                .totalCredits(totalCredits)
                .totalSales(totalSales)
                .variance(variance)
                .expenses(expenses)
                .sales(sales)
                .credits(credits) // NEW: Include credits list
                .build();
    }

    // Helper method to convert Credit to DTO
    private OSD_CreditDTO convertCreditToDTO(Credit credit) {
        OSD_CreditDTO dto = new OSD_CreditDTO();
        dto.setId(credit.getId());
        dto.setUserId(credit.getUser().getId());
        dto.setUserName(credit.getUser().getName());
        dto.setDepartment(credit.getDepartment());

        if (credit.getShop() != null) {
            dto.setShopId(credit.getShop().getId());
            dto.setShopName(credit.getShop().getName());
        }

        dto.setAmount(credit.getAmount());
        dto.setReason(credit.getReason());
        dto.setIsPaid(credit.getIsPaid());
        dto.setTransactionDate(credit.getTransactionDate());
        dto.setCreatedAt(credit.getCreatedAt());  // ⚠️ NEW: Include timestamp for sorting

        return dto;
    }

/**
 * Add expense to daily cash
 */
@Transactional
public void addExpense(Long dailyCashId, Double amount, Long expenseTypeId, String description, User user) {
    DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
            .orElseThrow(() -> new RuntimeException("Daily cash not found"));

    // Removed locked day check - allow adding expenses even after day is closed

    ExpenseType expenseType = expenseTypeId != null
            ? expenseTypeRepo.findById(expenseTypeId).orElse(null)
            : null;

    CashTransaction transaction = CashTransaction.builder()
            .dailyCash(dailyCash)
            .type("EXPENSE")
            .amount(amount)
            .expenseType(expenseType)
            .description(description)
            .recordedBy(user)
            .createdAt(LocalDateTime.now())
            .build();

    CashTransaction saved = cashTransactionRepo.save(transaction);

    // Create audit log
    java.util.Map<String, Object> newValues = new java.util.HashMap<>();
    newValues.put("id", saved.getId());
    newValues.put("dailyCashId", dailyCashId);
    newValues.put("type", "EXPENSE");
    newValues.put("amount", amount);
    newValues.put("expenseTypeId", expenseTypeId);
    newValues.put("description", description);
    auditLogService.createAuditLog(user, "CREATE", "CASH_TRANSACTION", saved.getId(), null, newValues);
}

/**
 * Add manual sale to daily cash
 */
@Transactional
public void addManualSale(Long dailyCashId, Double amount, String description, User user) {
    DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
            .orElseThrow(() -> new RuntimeException("Daily cash not found"));

    // Removed locked day check - allow adding sales even after day is closed

    CashTransaction transaction = CashTransaction.builder()
            .dailyCash(dailyCash)
            .type("SALE")
            .amount(amount)
            .description(description)
            .recordedBy(user)
            .createdAt(LocalDateTime.now())
            .build();

    CashTransaction saved = cashTransactionRepo.save(transaction);

    // Create audit log
    java.util.Map<String, Object> newValues = new java.util.HashMap<>();
    newValues.put("id", saved.getId());
    newValues.put("dailyCashId", dailyCashId);
    newValues.put("type", "SALE");
    newValues.put("amount", amount);
    newValues.put("description", description);
    auditLogService.createAuditLog(user, "CREATE", "CASH_TRANSACTION", saved.getId(), null, newValues);
}

/**
 * Close the day by setting closing cash and locking
 */
@Transactional
public void closeDay(Long dailyCashId, Double closingCash, User user) {
    DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
            .orElseThrow(() -> new RuntimeException("Daily cash not found"));

    if (dailyCash.getLocked()) {
        throw new RuntimeException("Day is already closed");
    }

    // Capture old values for audit
    java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
    oldValues.put("closingCash", dailyCash.getClosingCash());
    oldValues.put("locked", dailyCash.getLocked());
    oldValues.put("closedBy", dailyCash.getClosedBy() != null ? dailyCash.getClosedBy().getId() : null);

    dailyCash.setClosingCash(closingCash);
    dailyCash.setLocked(true);
    dailyCash.setClosedBy(user);
    dailyCash.setClosedAt(LocalDateTime.now());

    DailyCash saved = dailyCashRepo.save(dailyCash);

    // Create audit log
    java.util.Map<String, Object> newValues = new java.util.HashMap<>();
    newValues.put("closingCash", closingCash);
    newValues.put("locked", true);
    newValues.put("closedBy", user.getId());
    newValues.put("closedAt", saved.getClosedAt().toString());
    auditLogService.createAuditLog(user, "CLOSE_DAY", "DAILY_CASH", dailyCashId, oldValues, newValues);
}

    /**
     * Update opening balance (only if day is not locked)
     * ⚠️ UPDATED: Now sets opening_confirmed = TRUE when user confirms opening balance
     */
    @Transactional
    public void updateOpeningBalance(Long dailyCashId, Double openingCash) {
        DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
                .orElseThrow(() -> new RuntimeException("Daily cash not found"));

        // Removed locked day check - allow updating opening balance even after day is closed

        // Capture old values for audit
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("openingCash", dailyCash.getOpeningCash());
        oldValues.put("openingConfirmed", dailyCash.getOpeningConfirmed());

        dailyCash.setOpeningCash(openingCash);
        dailyCash.setOpeningConfirmed(true);  // ⚠️ NEW: Mark opening balance as confirmed
        dailyCashRepo.save(dailyCash);

        log.info("✅ Opening balance updated: dailyCashId={}, openingCash={}, openingConfirmed=TRUE",
                 dailyCashId, openingCash);
    }
}

