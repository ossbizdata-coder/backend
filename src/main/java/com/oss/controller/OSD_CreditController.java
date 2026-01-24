package com.oss.controller;

import com.oss.dto.OSD_CreditDTO;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import com.oss.service.CreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/credits")
public class OSD_CreditController {

    private static final Logger log = LoggerFactory.getLogger(OSD_CreditController.class);

    @Autowired
    private CreditService creditService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<OSD_CreditDTO> getAllCredits() {
        return creditService.getAllCredits();
    }

    @GetMapping("/filter")
    public List<OSD_CreditDTO> filterCredits(@RequestParam(required = false) Boolean isPaid) {
        return creditService.getCreditsByPaidStatus(isPaid);
    }

    @PostMapping
    public ResponseEntity<?> addCredit(@RequestBody Map<String, Object> body) {
        creditService.saveCredit(body);
        return ResponseEntity.ok("Credit added successfully");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        log.info("PATCH /api/credits/{} - Request body: {}", id, body);

        try {
            if (!body.containsKey("isPaid")) {
                log.warn("Missing isPaid field in request body");
                return ResponseEntity.badRequest().body("Missing isPaid field");
            }

            Boolean isPaid = (Boolean) body.get("isPaid");
            if (isPaid == null) {
                log.warn("isPaid field is null");
                return ResponseEntity.badRequest().body("isPaid cannot be null");
            }

            log.info("Updating credit {} to isPaid={}", id, isPaid);
            creditService.updatePaidStatus(id, isPaid);
            log.info("Successfully updated credit {} status", id);

            return ResponseEntity.ok("Status updated");

        } catch (RuntimeException e) {
            log.error("Runtime exception updating credit {}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Exception updating credit {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/summary")
    public List<Map<String, Object>> getSummary() {
        return creditService.getUnpaidSummary();
    }

    @GetMapping("/outstanding-total")
    public ResponseEntity<?> getOutstandingTotal() {
        Map<String, Object> response = creditService.getOutstandingTotal();
        return ResponseEntity.ok(response);
    }

    // ==================== STAFF SALARY CALCULATION ENDPOINTS ====================

    /**
     * Get all credits for a specific user (staff member)
     * GET /api/credits/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OSD_CreditDTO>> getCreditsByUserId(@PathVariable Long userId) {
        List<OSD_CreditDTO> credits = creditService.getCreditsByUserId(userId);
        return ResponseEntity.ok(credits);
    }

    /**
     * Get credits for a user filtered by paid status
     * GET /api/credits/user/{userId}/filter?isPaid=true
     */
    @GetMapping("/user/{userId}/filter")
    public ResponseEntity<List<OSD_CreditDTO>> getCreditsByUserIdAndPaidStatus(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean isPaid) {
        if (isPaid == null) {
            return ResponseEntity.ok(creditService.getCreditsByUserId(userId));
        }
        List<OSD_CreditDTO> credits = creditService.getCreditsByUserIdAndPaidStatus(userId, isPaid);
        return ResponseEntity.ok(credits);
    }

    /**
     * Get credits for a user within a date range
     * GET /api/credits/user/{userId}/range?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<OSD_CreditDTO>> getCreditsByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        List<OSD_CreditDTO> credits = creditService.getCreditsByUserIdAndDateRange(userId, start, end);
        return ResponseEntity.ok(credits);
    }

    /**
     * Get credit summary for a user (all time totals)
     * GET /api/credits/user/{userId}/summary
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getUserCreditSummary(@PathVariable Long userId) {
        Map<String, Object> summary = creditService.getUserCreditSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get daily credit total for a user (for daily salary calculation)
     * GET /api/credits/user/{userId}/daily/2026-01-15
     */
    @GetMapping("/user/{userId}/daily/{date}")
    public ResponseEntity<Map<String, Object>> getDailyCreditTotal(
            @PathVariable Long userId,
            @PathVariable String date) {
        java.time.LocalDate localDate = java.time.LocalDate.parse(date);
        Map<String, Object> response = creditService.getDailyCreditTotal(userId, localDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly credit total for a user (for monthly salary calculation)
     * GET /api/credits/user/{userId}/monthly/2026/1
     */
    @GetMapping("/user/{userId}/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyCreditTotal(
            @PathVariable Long userId,
            @PathVariable int year,
            @PathVariable int month) {
        Map<String, Object> response = creditService.getMonthlyCreditTotal(userId, year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Get daily breakdown of credits for a user
     * GET /api/credits/user/{userId}/breakdown?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/user/{userId}/breakdown")
    public ResponseEntity<Map<String, Object>> getDailyBreakdown(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        Map<String, Object> response = creditService.getDailyBreakdown(userId, start, end);
        return ResponseEntity.ok(response);
    }

    /**
     * Calculate salary with credit deductions
     * GET /api/credits/user/{userId}/salary/{year}/{month}?baseSalary=50000
     */
    @GetMapping("/user/{userId}/salary/{year}/{month}")
    public ResponseEntity<Map<String, Object>> calculateSalaryWithDeductions(
            @PathVariable Long userId,
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam Double baseSalary) {
        Map<String, Object> response = creditService.calculateSalaryWithDeductions(userId, year, month, baseSalary);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/credits/{id}
     * Delete a credit record (SUPERADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deleteCredit(@PathVariable Long id, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            creditService.deleteCredit(id, user);
            return ResponseEntity.ok("Credit deleted successfully");
        } catch (RuntimeException e) {
            log.error("Error deleting credit {}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * PUT /api/credits/{id}
     * Edit a credit record (SUPERADMIN only)
     */
    @PutMapping("/{id}/edit")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> editCredit(@PathVariable Long id, @RequestBody Map<String, Object> body, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            creditService.editCredit(id, body, user);
            return ResponseEntity.ok("Credit updated successfully");
        } catch (RuntimeException e) {
            log.error("Error updating credit {}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}