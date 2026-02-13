package com.oss.controller;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import com.oss.service.DailyCashService;
import com.oss.service.DailySummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;
/**
 * Admin Controller for editing/deleting daily cash records
 * SUPERADMIN only - for data corrections
 */
@RestController
@RequestMapping("/api/admin/daily-cash")
public class Admin_DailyCashController {
    private final DailyCashService dailyCashService;
    private final DailySummaryService dailySummaryService;
    private final UserRepository userRepository;
    public Admin_DailyCashController(DailyCashService dailyCashService,
                                    DailySummaryService dailySummaryService,
                                    UserRepository userRepository) {
        this.dailyCashService = dailyCashService;
        this.dailySummaryService = dailySummaryService;
        this.userRepository = userRepository;
    }
    /**
     * PUT /api/admin/daily-cash/{id}
     * Edit daily cash record (SUPERADMIN only)
     * Can update opening cash, closing cash, etc.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> updateDailyCash(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dailyCashService.updateDailyCash(id, updates, user);
            return ResponseEntity.ok(Map.of(
                "message", "Daily cash updated successfully",
                "note", "Daily summary has been recalculated"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    /**
     * DELETE /api/admin/daily-cash/{id}
     * Delete daily cash record (SUPERADMIN only)
     * WARNING: This will cascade delete all transactions for that day!
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> deleteDailyCash(
            @PathVariable Long id,
            Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dailyCashService.deleteDailyCash(id, user);
            return ResponseEntity.ok(Map.of(
                "message", "Daily cash deleted successfully",
                "warning", "All associated transactions were also deleted"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    /**
     * POST /api/admin/daily-cash/{id}/recalculate-summary
     * Manually recalculate daily summary for a specific day (SUPERADMIN only)
     * Useful if summary gets out of sync
     */
    @PostMapping("/{id}/recalculate-summary")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> recalculateSummary(@PathVariable Long id) {
        try {
            dailyCashService.recalculateSummaryForDay(id);
            return ResponseEntity.ok(Map.of(
                "message", "Daily summary recalculated successfully",
                "dailyCashId", id
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/admin/daily-cash/recalculate-all
     * Recalculate ALL daily summaries (SUPERADMIN only)
     * Use this after fixing calculation bugs
     */
    @PostMapping("/recalculate-all")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> recalculateAllSummaries() {
        try {
            int count = dailyCashService.recalculateAllSummaries();
            return ResponseEntity.ok(Map.of(
                "message", "All daily summaries recalculated successfully",
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}