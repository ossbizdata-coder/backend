package com.oss.controller;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import com.oss.service.CashTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;
/**
 * Admin Controller for editing/deleting cash transactions
 * SUPERADMIN only - for data corrections
 */
@RestController
@RequestMapping("/api/admin/transactions")
public class Admin_TransactionController {
    private final CashTransactionService transactionService;
    private final UserRepository userRepository;
    public Admin_TransactionController(CashTransactionService transactionService,
                                      UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }
    /**
     * PUT /api/admin/transactions/{id}
     * Edit a cash transaction (SUPERADMIN only)
     * Can update amount, description, expense type, etc.
     *
     * Request body example:
     * {
     *   "amount": 6000.00,
     *   "description": "Updated expense description",
     *   "expenseTypeId": 71
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> updateTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            transactionService.updateTransaction(id, updates, user);
            return ResponseEntity.ok(Map.of(
                "message", "Transaction updated successfully",
                "note", "Daily summary has been recalculated"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    /**
     * DELETE /api/admin/transactions/{id}
     * Delete a cash transaction (SUPERADMIN only)
     * Daily summary will be recalculated automatically
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Long id,
            Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            transactionService.deleteTransaction(id, user);
            return ResponseEntity.ok(Map.of(
                "message", "Transaction deleted successfully",
                "note", "Daily summary has been recalculated"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    /**
     * GET /api/admin/transactions/{id}
     * Get transaction details for editing (SUPERADMIN only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getTransaction(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transactionService.getTransactionDetails(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}