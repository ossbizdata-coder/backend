package com.oss.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oss.dto.OSD_TransactionUpdateRequest;
import com.oss.model.AuditLog;
import com.oss.model.Transaction;
import com.oss.model.User;
import com.oss.repository.AuditLogRepository;
import com.oss.repository.ExpenseTypeRepository;
import com.oss.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
@Service
public class OSD_TransactionService {
    private final TransactionRepository transactionRepo;
    private final ExpenseTypeRepository typeRepo;
    private final AuditLogRepository auditLogRepo;
    private final ObjectMapper objectMapper;
    public OSD_TransactionService(TransactionRepository transactionRepo,
                                  ExpenseTypeRepository typeRepo,
                                  AuditLogRepository auditLogRepo) {
        this.transactionRepo = transactionRepo;
        this.typeRepo = typeRepo;
        this.auditLogRepo = auditLogRepo;
        this.objectMapper = new ObjectMapper();
    }
    @Transactional
    public void saveTransaction(Map<String, Object> body, User user) {
        Instant nowUtc = Instant.now();
        Transaction t = new Transaction();
        // Support both shopType and department
        if (body.containsKey("department")) {
            t.setDepartment((String) body.get("department"));
            t.setShopType((String) body.get("department")); // Keep shopType for backward compatibility
        } else if (body.containsKey("shopType")) {
            t.setShopType((String) body.get("shopType"));
            t.setDepartment((String) body.get("shopType"));
        }
        t.setCategory((String) body.get("category"));
        t.setItemName((String) body.get("item"));
        t.setAmount(Double.parseDouble(body.get("amount").toString()));
        t.setUser(user);
        // Handle optional comment field
        if (body.containsKey("comment")) {
            t.setComment((String) body.get("comment"));
        }
        // Handle sales-specific fields
        if (body.containsKey("openingBalance")) {
            t.setOpeningBalance(Double.parseDouble(body.get("openingBalance").toString()));
        }
        if (body.containsKey("closingBalance")) {
            t.setClosingBalance(Double.parseDouble(body.get("closingBalance").toString()));
        }
        if (body.containsKey("totalExpenses")) {
            t.setTotalExpenses(Double.parseDouble(body.get("totalExpenses").toString()));
        }
        t.setTransactionTime(nowUtc);
        t.setCreatedAt(nowUtc);
        // Support custom date field for business_date
        // Store as epoch milliseconds (UTC midnight)
        if (body.containsKey("date")) {
            String dateStr = (String) body.get("date");
            java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr);
            Long businessDateMillis = localDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli();
            t.setBusinessDate(businessDateMillis);
        } else {
            // Use UTC midnight of today
            java.time.LocalDate today = nowUtc.atZone(java.time.ZoneId.of("UTC")).toLocalDate();
            Long businessDateMillis = today.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli();
            t.setBusinessDate(businessDateMillis);
        }
        if ("EXPENSE".equals(t.getCategory()) && body.containsKey("expenseTypeId")) {
            Long typeId = Long.valueOf(body.get("expenseTypeId").toString());
            t.setExpenseType(typeRepo.findById(typeId).orElseThrow());
        } else {
            t.setExpenseType(null);
        }
        transactionRepo.save(t);
    }
    @Transactional
    public Transaction updateTransaction(Long transactionId, OSD_TransactionUpdateRequest request, User user) {
        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        // Capture old values for audit log
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("amount", transaction.getAmount());
        oldValues.put("itemName", transaction.getItemName());
        oldValues.put("comment", transaction.getComment());
        oldValues.put("expenseTypeId", transaction.getExpenseType() != null ? transaction.getExpenseType().getId() : null);
        // Update fields if provided
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setItemName(request.getDescription());
        }
        if (request.getComment() != null) {
            transaction.setComment(request.getComment());
        }
        if (request.getExpenseTypeId() != null) {
            transaction.setExpenseType(typeRepo.findById(request.getExpenseTypeId())
                    .orElseThrow(() -> new RuntimeException("Expense type not found")));
        }
        Transaction updated = transactionRepo.save(transaction);
        // Capture new values for audit log
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("amount", updated.getAmount());
        newValues.put("itemName", updated.getItemName());
        newValues.put("comment", updated.getComment());
        newValues.put("expenseTypeId", updated.getExpenseType() != null ? updated.getExpenseType().getId() : null);
        // Create audit log
        createAuditLog(user, "EDIT", "TRANSACTION", transactionId, oldValues, newValues);
        return updated;
    }
    @Transactional
    public void deleteTransaction(Long transactionId, User user) {
        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        // Capture values for audit log before deletion
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("id", transaction.getId());
        oldValues.put("amount", transaction.getAmount());
        oldValues.put("category", transaction.getCategory());
        oldValues.put("itemName", transaction.getItemName());
        oldValues.put("department", transaction.getDepartment());
        oldValues.put("comment", transaction.getComment());
        oldValues.put("businessDate", transaction.getBusinessDate());
        // Create audit log before deletion
        createAuditLog(user, "DELETE", "TRANSACTION", transactionId, oldValues, null);
        // Delete the transaction
        transactionRepo.delete(transaction);
    }
    private void createAuditLog(User user, String action, String entityType, Long entityId,
                                Map<String, Object> oldValues, Map<String, Object> newValues) {
        try {
            String oldValueJson = oldValues != null ? objectMapper.writeValueAsString(oldValues) : null;
            String newValueJson = newValues != null ? objectMapper.writeValueAsString(newValues) : null;
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValueJson)
                    .newValue(newValueJson)
                    .createdAt(Instant.now())
                    .build();
            auditLogRepo.save(auditLog);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
            // Don't fail the transaction if audit logging fails
        }
    }
}