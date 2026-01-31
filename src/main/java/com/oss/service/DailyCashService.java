package com.oss.service;
import com.oss.dto.CashTransactionDTO;
import com.oss.dto.DailyCashSummaryDTO;
import com.oss.dto.OSD_CreditDTO;
import com.oss.dto.ShopSummaryDTO;
import com.oss.model.*;
import com.oss.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import com.oss.repository.UserRepository;
@Service
public class DailyCashService {
    private static final Logger log = LoggerFactory.getLogger(DailyCashService.class);

    private final DailyCashRepository dailyCashRepo;
    private final ShopRepository shopRepo;
    private final CashTransactionRepository cashTransactionRepo;
    private final CreditRepository creditRepo;
    private final ExpenseTypeRepository expenseTypeRepo;
    private final AuditLogService auditLogService;
    private final DailySummaryService dailySummaryService;
    private final UserRepository userRepository;
    public DailyCashService(DailyCashRepository dailyCashRepo,
                           ShopRepository shopRepo,
                           CashTransactionRepository cashTransactionRepo,
                           CreditRepository creditRepo,
                           ExpenseTypeRepository expenseTypeRepo,
                           AuditLogService auditLogService,
                           DailySummaryService dailySummaryService,
                           UserRepository userRepository) {
        this.dailyCashRepo = dailyCashRepo;
        this.shopRepo = shopRepo;
        this.cashTransactionRepo = cashTransactionRepo;
        this.creditRepo = creditRepo;
        this.expenseTypeRepo = expenseTypeRepo;
        this.auditLogService = auditLogService;
        this.dailySummaryService = dailySummaryService;
        this.userRepository = userRepository;
    }
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
    @Transactional
    public DailyCash getOrCreateDailyCash(Long shopId, LocalDate date) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        Optional<DailyCash> existing = dailyCashRepo.findByShopAndBusinessDate(shop, date);
        if (existing.isPresent()) {
            return existing.get();
        }
        synchronized (this) {
            existing = dailyCashRepo.findByShopAndBusinessDate(shop, date);
            if (existing.isPresent()) {
                return existing.get();
            }
            double openingCash = 0.0;
            try {
                DailyCash previousDay = dailyCashRepo.findLatestPreviousByShopIdAndDate(shopId, date);
                if (previousDay != null && previousDay.getClosingCash() != null) {
                    openingCash = previousDay.getClosingCash();
                }
            } catch (Exception e) {
                openingCash = 0.0;
            }
            DailyCash newDailyCash = DailyCash.builder()
                    .shop(shop)
                    .businessDate(date)
                    .openingCash(openingCash)
                    .openingConfirmed(false)
                    .locked(false)
                    .build();
            return dailyCashRepo.save(newDailyCash);
        }
    }
    public List<DailyCash> findLatestClosedByShopAndDateRange(Long shopId, LocalDate startDate, LocalDate endDate) {
        return dailyCashRepo.findLatestClosedByShopAndDateRange(shopId, startDate, endDate);
    }
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
            // Total Sales = all sales including both cash and credit
            // Formula: totalSales = (closingCash - openingCash) + totalExpenses + totalCredits - manualSales
            totalSales = (dailyCash.getClosingCash() - dailyCash.getOpeningCash()) + totalExpenses + totalCredits - manualSales;

            // Expected closing = opening + totalSales - totalExpenses - totalCredits + manualSales
            Double expectedClosing = dailyCash.getOpeningCash() + totalSales - totalExpenses - totalCredits + manualSales;

            // Variance = difference between expected and actual closing cash
            // Positive variance = more cash than expected (surplus)
            // Negative variance = less cash than expected (shortfall)
            variance = dailyCash.getClosingCash() - expectedClosing;
        }
        return DailyCashSummaryDTO.builder()
                .dailyCashId(dailyCash.getId())
                .shopId(dailyCash.getShop().getId())
                .shopCode(dailyCash.getShop().getCode())
                .shopName(dailyCash.getShop().getName())
                .businessDate(dailyCash.getBusinessDate())
                .openingCash(dailyCash.getOpeningCash())
                .openingConfirmed(dailyCash.getOpeningConfirmed())  // âš ï¸ NEW: Return opening confirmed status
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
        dto.setCreatedAt(credit.getCreatedAt());  // âš ï¸ NEW: Include timestamp for sorting
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
    // âœ… NEW: Automatically calculate and save daily summary for performance optimization
    try {
        dailySummaryService.calculateAndSaveDailySummary(saved);
    } catch (Exception e) {
        log.error("Failed to calculate daily summary: {}", e.getMessage(), e);
        // Don't fail the transaction if summary calculation fails
    }
}
    /**
     * Update opening balance (only if day is not locked)
     * âš ï¸ UPDATED: Now sets opening_confirmed = TRUE when user confirms opening balance
     */
    @Transactional
    public void updateOpeningBalance(Long dailyCashId, Double openingCash) {
        // Resolve authenticated user from security context and delegate
        String username = null;
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }
        }
        if (username == null) {
            throw new RuntimeException("Unauthenticated request");
        }
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Invalid user"));
        updateOpeningBalance(dailyCashId, openingCash, user);
    }

    @Transactional
    public void updateOpeningBalance(Long dailyCashId, Double openingCash, User user) {
        DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
                .orElseThrow(() -> new RuntimeException("Daily cash not found"));

        // Capture old values for audit (only if updating existing confirmed balance)
        java.util.Map<String, Object> oldValues = null;
        boolean isFirstTime = !Boolean.TRUE.equals(dailyCash.getOpeningConfirmed());

        if (!isFirstTime && dailyCash.getOpeningCash() != null) {
            oldValues = new java.util.HashMap<>();
            oldValues.put("id", dailyCash.getId());
            oldValues.put("openingCash", dailyCash.getOpeningCash());
            oldValues.put("openingConfirmed", dailyCash.getOpeningConfirmed());
        }

        // Update opening balance
        dailyCash.setOpeningCash(openingCash);
        dailyCash.setOpeningConfirmed(true);
        DailyCash saved = dailyCashRepo.save(dailyCash);

        // Prepare new values for audit (include full context per spec)
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("id", saved.getId());
        newValues.put("shopId", saved.getShop().getId());
        newValues.put("businessDate", saved.getBusinessDate().toString());
        newValues.put("openingCash", saved.getOpeningCash());
        newValues.put("openingConfirmed", saved.getOpeningConfirmed());

        // Add openedBy nested object
        java.util.Map<String, Object> openedBy = new java.util.HashMap<>();
        openedBy.put("id", user.getId());
        openedBy.put("name", user.getName());
        newValues.put("openedBy", openedBy);

        // Use "EDIT" action as per specification (not ADD_START/UPDATE_OPENING)
        auditLogService.createAuditLog(user, "EDIT", "DAILY_CASH", dailyCashId, oldValues, newValues);
    }
    /**
     * Update daily cash record (SUPERADMIN only)
     * Can update opening cash, closing cash, etc.
     * Automatically recalculates daily summary
     */
    @Transactional
    public void updateDailyCash(Long dailyCashId, Map<String, Object> updates, User user) {
        DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
                .orElseThrow(() -> new RuntimeException("Daily cash not found"));
        // Capture old values for audit
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("openingCash", dailyCash.getOpeningCash());
        oldValues.put("closingCash", dailyCash.getClosingCash());
        oldValues.put("locked", dailyCash.getLocked());
        // Update fields
        if (updates.containsKey("openingCash")) {
            dailyCash.setOpeningCash(((Number) updates.get("openingCash")).doubleValue());
        }
        if (updates.containsKey("closingCash")) {
            dailyCash.setClosingCash(((Number) updates.get("closingCash")).doubleValue());
        }
        if (updates.containsKey("locked")) {
            dailyCash.setLocked((Boolean) updates.get("locked"));
        }
        if (updates.containsKey("openingConfirmed")) {
            dailyCash.setOpeningConfirmed((Boolean) updates.get("openingConfirmed"));
        }
        DailyCash saved = dailyCashRepo.save(dailyCash);
        // Create audit log
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("openingCash", saved.getOpeningCash());
        newValues.put("closingCash", saved.getClosingCash());
        newValues.put("locked", saved.getLocked());
        auditLogService.createAuditLog(user, "EDIT", "DAILY_CASH", dailyCashId, oldValues, newValues);
        // âœ… Recalculate summary if day is closed
        if (saved.getLocked()) {
            try {
                dailySummaryService.calculateAndSaveDailySummary(saved);
            } catch (Exception e) {
                log.error("Failed to recalculate daily summary: {}", e.getMessage(), e);
            }
        }
    }
    /**
     * Delete daily cash record (SUPERADMIN only)
     * WARNING: This will cascade delete all transactions!
     */
    @Transactional
    public void deleteDailyCash(Long dailyCashId, User user) {
        DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
                .orElseThrow(() -> new RuntimeException("Daily cash not found"));
        // Capture old values for audit
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("shopId", dailyCash.getShop().getId());
        oldValues.put("shopName", dailyCash.getShop().getName());
        oldValues.put("businessDate", dailyCash.getBusinessDate().toString());
        oldValues.put("openingCash", dailyCash.getOpeningCash());
        oldValues.put("closingCash", dailyCash.getClosingCash());
        oldValues.put("locked", dailyCash.getLocked());
        // Audit log before deletion
        auditLogService.createAuditLog(user, "DELETE", "DAILY_CASH", dailyCashId, oldValues, null);
        // Delete daily cash (transactions will cascade if configured)
        dailyCashRepo.delete(dailyCash);
    }
    /**
     * Manually recalculate daily summary for a specific day (SUPERADMIN only)
     */
    @Transactional
    public void recalculateSummaryForDay(Long dailyCashId) {
        DailyCash dailyCash = dailyCashRepo.findById(dailyCashId)
                .orElseThrow(() -> new RuntimeException("Daily cash not found"));
        if (!dailyCash.getLocked()) {
            throw new RuntimeException("Cannot recalculate summary for open day");
        }
        dailySummaryService.calculateAndSaveDailySummary(dailyCash);
    }
}

