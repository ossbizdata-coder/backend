package com.oss.service;
import com.oss.model.CashTransaction;
import com.oss.model.DailyCash;
import com.oss.model.ExpenseType;
import com.oss.model.User;
import com.oss.repository.CashTransactionRepository;
import com.oss.repository.DailyCashRepository;
import com.oss.repository.ExpenseTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
/**
 * Service for managing cash transactions (expenses and manual sales)
 * Includes edit/delete capabilities for SUPERADMIN
 */
@Service
public class CashTransactionService {
    private static final Logger log = LoggerFactory.getLogger(CashTransactionService.class);

    private final CashTransactionRepository transactionRepo;
    private final DailyCashRepository dailyCashRepo;
    private final ExpenseTypeRepository expenseTypeRepo;
    private final AuditLogService auditLogService;
    private final DailySummaryService dailySummaryService;
    public CashTransactionService(CashTransactionRepository transactionRepo,
                                 DailyCashRepository dailyCashRepo,
                                 ExpenseTypeRepository expenseTypeRepo,
                                 AuditLogService auditLogService,
                                 DailySummaryService dailySummaryService) {
        this.transactionRepo = transactionRepo;
        this.dailyCashRepo = dailyCashRepo;
        this.expenseTypeRepo = expenseTypeRepo;
        this.auditLogService = auditLogService;
        this.dailySummaryService = dailySummaryService;
    }
    public Map<String, Object> getTransactionDetails(Long id) {
        CashTransaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        Map<String, Object> details = new HashMap<>();
        details.put("id", transaction.getId());
        details.put("dailyCashId", transaction.getDailyCash().getId());
        details.put("type", transaction.getType());
        details.put("amount", transaction.getAmount());
        details.put("description", transaction.getDescription());
        details.put("expenseTypeId", transaction.getExpenseType() != null ? transaction.getExpenseType().getId() : null);
        details.put("expenseTypeName", transaction.getExpenseType() != null ? transaction.getExpenseType().getName() : null);
        details.put("recordedBy", transaction.getRecordedBy() != null ? transaction.getRecordedBy().getName() : null);
        details.put("createdAt", transaction.getCreatedAt().toString());
        details.put("shopCode", transaction.getDailyCash().getShop().getCode());
        details.put("businessDate", transaction.getDailyCash().getBusinessDate().toString());
        return details;
    }
    /**
     * Update a cash transaction (SUPERADMIN only)
     * Automatically recalculates daily summary
     */
    @Transactional
    public void updateTransaction(Long id, Map<String, Object> updates, User user) {
        CashTransaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        DailyCash dailyCash = transaction.getDailyCash();
        // Capture old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("amount", transaction.getAmount());
        oldValues.put("description", transaction.getDescription());
        oldValues.put("expenseTypeId", transaction.getExpenseType() != null ? transaction.getExpenseType().getId() : null);
        // Update fields
        if (updates.containsKey("amount")) {
            transaction.setAmount(((Number) updates.get("amount")).doubleValue());
        }
        if (updates.containsKey("description")) {
            transaction.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("expenseTypeId")) {
            Long expenseTypeId = ((Number) updates.get("expenseTypeId")).longValue();
            ExpenseType expenseType = expenseTypeRepo.findById(expenseTypeId)
                    .orElseThrow(() -> new RuntimeException("Expense type not found"));
            transaction.setExpenseType(expenseType);
        }
        CashTransaction saved = transactionRepo.save(transaction);
        // Create audit log
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("amount", saved.getAmount());
        newValues.put("description", saved.getDescription());
        newValues.put("expenseTypeId", saved.getExpenseType() != null ? saved.getExpenseType().getId() : null);
        auditLogService.createAuditLog(user, "EDIT", "CASH_TRANSACTION", id, oldValues, newValues);
        // âœ… RECALCULATE SUMMARY if day is closed
        if (dailyCash.getLocked()) {
            try {
                dailySummaryService.calculateAndSaveDailySummary(dailyCash);
            } catch (Exception e) {
                log.error("Failed to recalculate daily summary: {}", e.getMessage(), e);
            }
        }
    }
    /**
     * Delete a cash transaction (SUPERADMIN only)
     * Automatically recalculates daily summary
     */
    @Transactional
    public void deleteTransaction(Long id, User user) {
        CashTransaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        // Save reference to daily cash before deletion
        DailyCash dailyCash = transaction.getDailyCash();
        boolean wasClosed = dailyCash.getLocked();
        // Capture old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("dailyCashId", dailyCash.getId());
        oldValues.put("type", transaction.getType());
        oldValues.put("amount", transaction.getAmount());
        oldValues.put("description", transaction.getDescription());
        oldValues.put("expenseTypeId", transaction.getExpenseType() != null ? transaction.getExpenseType().getId() : null);
        // Audit log before deletion
        auditLogService.createAuditLog(user, "DELETE", "CASH_TRANSACTION", id, oldValues, null);
        // Delete transaction
        transactionRepo.delete(transaction);
        // âœ… RECALCULATE SUMMARY if day is/was closed
        if (wasClosed) {
            try {
                dailySummaryService.calculateAndSaveDailySummary(dailyCash);
            } catch (Exception e) {
                log.error("Failed to recalculate daily summary: {}", e.getMessage(), e);
            }
        }
    }
}