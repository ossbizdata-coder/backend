package com.oss.service;
import com.oss.dto.OSD_CreditDTO;
import com.oss.model.Credit;
import com.oss.model.DailyCash;
import com.oss.model.User;
import com.oss.repository.CreditRepository;
import com.oss.repository.DailyCashRepository;
import com.oss.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class CreditService {
    private static final Logger log = LoggerFactory.getLogger(CreditService.class);
    @Autowired
    private CreditRepository creditRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private DailyCashRepository dailyCashRepository;
    @Autowired
    private DailySummaryService dailySummaryService;
    public List<OSD_CreditDTO> getAllCredits() {
        return creditRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<OSD_CreditDTO> getCreditsByPaidStatus(Boolean isPaid) {
        if (isPaid == null) {
            return getAllCredits();
        }
        return creditRepository.findByIsPaidOrderByCreatedAtDesc(isPaid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public void saveCredit(Map<String, Object> body) {
        // Validate required fields
        if (body.get("userId") == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (body.get("amount") == null) {
            throw new IllegalArgumentException("amount is required");
        }
        Long userId;
        Double amount;
        try {
            userId = Long.valueOf(body.get("userId").toString());
            amount = Double.valueOf(body.get("amount").toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId or amount format");
        }
        String reason = (String) body.get("reason");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Credit credit = new Credit();
        credit.setUser(user);
        credit.setAmount(amount);
        credit.setReason(reason);
        // Support department field (optional, defaults to COMMON)
        if (body.containsKey("department")) {
            credit.setDepartment((String) body.get("department"));
        } else {
            credit.setDepartment("COMMON");
        }
        // Support transactionDate field (optional, defaults to today)
        if (body.containsKey("transactionDate")) {
            String dateStr = (String) body.get("transactionDate");
            credit.setTransactionDate(java.time.LocalDate.parse(dateStr));
        } else if (body.containsKey("date")) {
            String dateStr = (String) body.get("date");
            credit.setTransactionDate(java.time.LocalDate.parse(dateStr));
        } else {
            credit.setTransactionDate(java.time.LocalDate.now());
        }
        Credit saved = creditRepository.save(credit);
        // Create audit log
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("id", saved.getId());
        newValues.put("userId", userId);
        newValues.put("amount", amount);
        newValues.put("reason", reason);
        newValues.put("department", saved.getDepartment());
        newValues.put("transactionDate", saved.getTransactionDate().toString());
        auditLogService.createAuditLog(user, "CREATE", "CREDIT", saved.getId(), null, newValues);
    }
    @Transactional
    public void updatePaidStatus(Long id, Boolean isPaid) {
        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Credit with id {} not found", id);
                    return new RuntimeException("Credit entry not found");
                });
        // Capture old values for audit
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("isPaid", credit.getIsPaid());
        credit.setIsPaid(isPaid);
        Credit saved = creditRepository.save(credit);
        // Create audit log
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("isPaid", isPaid);
        auditLogService.createAuditLog(credit.getUser(), "UPDATE_PAID_STATUS", "CREDIT", id, oldValues, newValues);
    }
    private OSD_CreditDTO convertToDTO(Credit credit) {
        OSD_CreditDTO dto = new OSD_CreditDTO();
        dto.setId(credit.getId());
        dto.setUserId(credit.getUser().getId());
        dto.setUserName(credit.getUser().getName());
        dto.setDepartment(credit.getDepartment());
        // NEW: Add shop information
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
    public List<Map<String, Object>> getUnpaidSummary() {
        // Groups by user and sums unpaid amounts
        return creditRepository.findAll().stream()
                .filter(c -> !c.getIsPaid())
                .collect(Collectors.groupingBy(
                        c -> c.getUser().getName(),
                        Collectors.summingDouble(Credit::getAmount)
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userName", entry.getKey());
                    map.put("totalUnpaid", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
    public Map<String, Object> getOutstandingTotal() {
        Double totalAmount = creditRepository.sumUnpaidCredits();
        Long count = creditRepository.countUnpaidCredits();
        Map<String, Object> response = new HashMap<>();
        response.put("totalAmount", totalAmount != null ? totalAmount : 0.0);
        response.put("count", count != null ? count : 0L);
        return response;
    }
    // ==================== STAFF SALARY CALCULATION METHODS ====================
    /**
     * Delete a credit record (SUPERADMIN only)
     */
    @Transactional
    public void deleteCredit(Long id, User user) {
        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit not found"));
        // Capture values for audit log before deletion
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("id", credit.getId());
        oldValues.put("userId", credit.getUser().getId());
        oldValues.put("userName", credit.getUser().getName());
        oldValues.put("amount", credit.getAmount());
        oldValues.put("reason", credit.getReason());
        oldValues.put("isPaid", credit.getIsPaid());
        oldValues.put("department", credit.getDepartment());
        oldValues.put("transactionDate", credit.getTransactionDate().toString());
        // Create audit log before deletion
        auditLogService.createAuditLog(user, "DELETE", "CREDIT", id, oldValues, null);
        // Save reference to shop and date before deletion for summary recalculation
        LocalDate creditDate = credit.getTransactionDate();
        Long shopId = credit.getShop() != null ? credit.getShop().getId() : null;
        // Delete the credit
        creditRepository.delete(credit);
        // âœ… RECALCULATE SUMMARY for the credit's date
        if (shopId != null) {
            DailyCash dailyCash = dailyCashRepository
                    .findByShop_IdAndBusinessDate(shopId, creditDate)
                    .orElse(null);
            if (dailyCash != null && dailyCash.getLocked()) {
                try {
                    dailySummaryService.calculateAndSaveDailySummary(dailyCash);
                } catch (Exception e) {
                    log.error("Failed to recalculate daily summary: {}", e.getMessage(), e);
                }
            }
        }
    }
    /**
     * Edit a credit record (SUPERADMIN only)
     */
    @Transactional
    public void editCredit(Long id, Map<String, Object> body, User user) {
        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit not found"));
        // Capture old values for audit
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("userId", credit.getUser().getId());
        oldValues.put("amount", credit.getAmount());
        oldValues.put("reason", credit.getReason());
        oldValues.put("isPaid", credit.getIsPaid());
        oldValues.put("department", credit.getDepartment());
        oldValues.put("transactionDate", credit.getTransactionDate().toString());
        // Update fields if provided
        if (body.containsKey("userId")) {
            Long newUserId = Long.valueOf(body.get("userId").toString());
            User newUser = userRepository.findById(newUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            credit.setUser(newUser);
        }
        if (body.containsKey("amount")) {
            credit.setAmount(Double.valueOf(body.get("amount").toString()));
        }
        if (body.containsKey("reason")) {
            credit.setReason((String) body.get("reason"));
        }
        if (body.containsKey("isPaid")) {
            credit.setIsPaid((Boolean) body.get("isPaid"));
        }
        if (body.containsKey("department")) {
            credit.setDepartment((String) body.get("department"));
        }
        if (body.containsKey("transactionDate")) {
            credit.setTransactionDate(java.time.LocalDate.parse((String) body.get("transactionDate")));
        }
        Credit saved = creditRepository.save(credit);
        // Capture new values for audit
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("userId", saved.getUser().getId());
        newValues.put("amount", saved.getAmount());
        newValues.put("reason", saved.getReason());
        newValues.put("isPaid", saved.getIsPaid());
        newValues.put("department", saved.getDepartment());
        newValues.put("transactionDate", saved.getTransactionDate().toString());
        // Create audit log
        auditLogService.createAuditLog(user, "EDIT", "CREDIT", id, oldValues, newValues);
        // âœ… RECALCULATE SUMMARY for the credit's date
        LocalDate creditDate = saved.getTransactionDate();
        if (saved.getShop() != null) {
            DailyCash dailyCash = dailyCashRepository
                    .findByShop_IdAndBusinessDate(saved.getShop().getId(), creditDate)
                    .orElse(null);
            if (dailyCash != null && dailyCash.getLocked()) {
                try {
                    dailySummaryService.calculateAndSaveDailySummary(dailyCash);
                } catch (Exception e) {
                    log.error("Failed to recalculate daily summary: {}", e.getMessage(), e);
                }
            }
        }
    }
}