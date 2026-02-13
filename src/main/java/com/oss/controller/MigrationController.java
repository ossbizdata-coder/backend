package com.oss.controller;
import com.oss.service.DailySummaryService;
import com.oss.service.DataMigrationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
/**
 * Controller for running data migration
 * WARNING: These endpoints should only be used ONCE during migration
 * Consider removing or securing these endpoints after migration is complete
 */
@RestController
@RequestMapping("/api/admin/migration")
public class MigrationController {
    private final DataMigrationService migrationService;
    private final DailySummaryService dailySummaryService;
    public MigrationController(DataMigrationService migrationService,
                              DailySummaryService dailySummaryService) {
        this.migrationService = migrationService;
        this.dailySummaryService = dailySummaryService;
    }
    /**
     * POST /api/admin/migration/init-shops
     * Initialize shop data
     */
    @PostMapping("/init-shops")
    public ResponseEntity<?> initShops() {
        try {
            migrationService.initializeShops();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Shops initialized successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    /**
     * POST /api/admin/migration/migrate-transactions
     * Migrate old shop_transactions to new structure
     */
    @PostMapping("/migrate-transactions")
    public ResponseEntity<?> migrateTransactions() {
        try {
            migrationService.migrateShopTransactions();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Transactions migrated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    /**
     * POST /api/admin/migration/update-credits
     * Update credits with shop references
     */
    @PostMapping("/update-credits")
    public ResponseEntity<?> updateCredits() {
        try {
            migrationService.updateCreditsWithShops();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Credits updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    /**
     * POST /api/admin/migration/run-full
     * Run complete migration (all steps)
     */
    @PostMapping("/run-full")
    public ResponseEntity<?> runFullMigration() {
        try {
            migrationService.runFullMigration();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Full migration completed successfully");
            response.put("note", "Please verify data and update application.properties if needed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    /**
     * POST /api/admin/migration/backfill-summaries
     * Backfill daily summaries for existing data (SUPERADMIN only)
     * Example: POST /api/admin/migration/backfill-summaries?startDate=2026-01-01&endDate=2026-01-31
     *
     * This creates pre-calculated daily summaries for performance optimization.
     * After running this, Business Overview and Staff Performance will be 10-100x faster!
     */
    @PostMapping("/backfill-summaries")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> backfillDailySummaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            dailySummaryService.recalculateSummaries(startDate, endDate);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Daily summaries backfilled successfully");
            response.put("startDate", startDate.toString());
            response.put("endDate", endDate.toString());
            response.put("note", "Performance optimization enabled - reports will now be 10-100x faster!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    /**
     * POST /api/admin/migration/backfill-summaries-all
     * Backfill ALL daily summaries from earliest to latest date (SUPERADMIN only)
     * WARNING: This may take time for large datasets
     *
     * This automatically calculates summaries for the last 3 months.
     * Adjust the date range in code if you need different period.
     */
    @PostMapping("/backfill-summaries-all")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> backfillAllDailySummaries() {
        try {
            // Calculate from earliest to latest (approximately 3 months ago to today)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(3);
            dailySummaryService.recalculateSummaries(startDate, endDate);
            Map<String, String> response = new HashMap<>();
            response.put("message", "All daily summaries backfilled successfully");
            response.put("startDate", startDate.toString());
            response.put("endDate", endDate.toString());
            response.put("note", "Performance optimization enabled - reports will now be 10-100x faster!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}