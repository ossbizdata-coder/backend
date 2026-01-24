package com.oss.service;

import com.oss.dto.OSD_CreditDTO;
import com.oss.model.Credit;
import com.oss.model.User;
import com.oss.repository.CreditRepository;
import com.oss.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.debug("Updating credit {} paid status to {}", id, isPaid);

        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Credit with id {} not found", id);
                    return new RuntimeException("Credit entry not found");
                });

        log.debug("Found credit {}: current isPaid={}, new isPaid={}", id, credit.getIsPaid(), isPaid);

        // Capture old values for audit
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("isPaid", credit.getIsPaid());

        credit.setIsPaid(isPaid);
        Credit saved = creditRepository.save(credit);

        // Create audit log
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("isPaid", isPaid);
        auditLogService.createAuditLog(credit.getUser(), "UPDATE_PAID_STATUS", "CREDIT", id, oldValues, newValues);

        log.info("Successfully updated credit {} to isPaid={}", saved.getId(), saved.getIsPaid());
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
        dto.setCreatedAt(credit.getCreatedAt());  // ⚠️ NEW: Include timestamp for sorting
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
     * Get all credits for a specific user (staff member)
     */
    public List<OSD_CreditDTO> getCreditsByUserId(Long userId) {
        return creditRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get credits for a user filtered by paid status
     */
    public List<OSD_CreditDTO> getCreditsByUserIdAndPaidStatus(Long userId, Boolean isPaid) {
        return creditRepository.findByUserIdAndPaidStatus(userId, isPaid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get credits for a user within a date range
     */
    public List<OSD_CreditDTO> getCreditsByUserIdAndDateRange(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return creditRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get total credit summary for a user (for salary calculation)
     */
    public Map<String, Object> getUserCreditSummary(Long userId) {
        Double totalCredits = creditRepository.sumCreditsByUserId(userId);
        Double unpaidCredits = creditRepository.sumUnpaidCreditsByUserId(userId);
        Double paidCredits = creditRepository.sumPaidCreditsByUserId(userId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("totalCredits", totalCredits != null ? totalCredits : 0.0);
        summary.put("unpaidCredits", unpaidCredits != null ? unpaidCredits : 0.0);
        summary.put("paidCredits", paidCredits != null ? paidCredits : 0.0);

        // Add user name if available
        userRepository.findById(userId).ifPresent(user -> {
            summary.put("userName", user.getName());
            summary.put("userEmail", user.getEmail());
        });

        return summary;
    }

    /**
     * Get daily credit total for a user (for daily salary calculation)
     */
    public Map<String, Object> getDailyCreditTotal(Long userId, java.time.LocalDate date) {
        Double dailyTotal = creditRepository.sumCreditsByUserIdAndDate(userId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("date", date.toString());
        response.put("totalCredits", dailyTotal != null ? dailyTotal : 0.0);

        // Add user name
        userRepository.findById(userId).ifPresent(user -> {
            response.put("userName", user.getName());
        });

        return response;
    }

    /**
     * Get monthly credit total for a user (for monthly salary calculation)
     */
    public Map<String, Object> getMonthlyCreditTotal(Long userId, int year, int month) {
        java.time.LocalDate startDate = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Double monthlyTotal = creditRepository.sumCreditsByUserIdAndDateRange(userId, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("year", year);
        response.put("month", month);
        response.put("startDate", startDate.toString());
        response.put("endDate", endDate.toString());
        response.put("totalCredits", monthlyTotal != null ? monthlyTotal : 0.0);

        // Add user name
        userRepository.findById(userId).ifPresent(user -> {
            response.put("userName", user.getName());
        });

        return response;
    }

    /**
     * Get daily breakdown of credits for a user within a date range
     */
    public Map<String, Object> getDailyBreakdown(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<Object[]> dailyData = creditRepository.sumCreditsByUserIdGroupedByDate(userId, startDate, endDate);

        List<Map<String, Object>> dailyBreakdown = dailyData.stream()
                .map(row -> {
                    Map<String, Object> day = new HashMap<>();
                    day.put("date", row[0].toString());
                    day.put("total", (Double) row[1]);
                    return day;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("startDate", startDate.toString());
        response.put("endDate", endDate.toString());
        response.put("dailyBreakdown", dailyBreakdown);

        // Calculate total
        Double total = dailyBreakdown.stream()
                .mapToDouble(day -> (Double) day.get("total"))
                .sum();
        response.put("totalCredits", total);

        // Add user name
        userRepository.findById(userId).ifPresent(user -> {
            response.put("userName", user.getName());
        });

        return response;
    }

    /**
     * Calculate salary deduction based on credits
     * This is a helper method that can be customized based on your salary calculation logic
     */
    public Map<String, Object> calculateSalaryWithDeductions(Long userId, int year, int month, Double baseSalary) {
        java.time.LocalDate startDate = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Double totalCredits = creditRepository.sumCreditsByUserIdAndDateRange(userId, startDate, endDate);
        Double unpaidCredits = creditRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .filter(c -> !c.getIsPaid())
                .mapToDouble(Credit::getAmount)
                .sum();

        Double netSalary = baseSalary - (totalCredits != null ? totalCredits : 0.0);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("year", year);
        response.put("month", month);
        response.put("baseSalary", baseSalary);
        response.put("totalCredits", totalCredits != null ? totalCredits : 0.0);
        response.put("unpaidCredits", unpaidCredits);
        response.put("netSalary", netSalary);

        // Add user name
        userRepository.findById(userId).ifPresent(user -> {
            response.put("userName", user.getName());
            response.put("userEmail", user.getEmail());
        });

        return response;
    }

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

        // Delete the credit
        creditRepository.delete(credit);
        log.info("Credit {} deleted by user {}", id, user.getName());
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
        log.info("Credit {} edited by user {}", id, user.getName());
    }
}