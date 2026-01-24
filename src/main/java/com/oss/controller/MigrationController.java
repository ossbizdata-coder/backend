package com.oss.controller;

import com.oss.service.DataMigrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public MigrationController(DataMigrationService migrationService) {
        this.migrationService = migrationService;
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
}

