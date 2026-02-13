package com.oss.controller;
import com.oss.model.AuditLog;
import com.oss.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    /**
     * GET /api/audit-logs
     * Get all audit logs (SUPERADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }
    /**
     * GET /api/audit-logs/entity/{entityType}/{entityId}
     * Get audit logs for a specific entity (e.g., DAILY_CASH/123)
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByEntity(entityType, entityId));
    }
    /**
     * GET /api/audit-logs/user/{userId}
     * Get audit logs for a specific user's actions
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAuditLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByUser(userId));
    }
    /**
     * GET /api/audit-logs/filter
     * Filter audit logs by entity type or action
     */
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<List<Map<String, Object>>> filterAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action) {
        return ResponseEntity.ok(auditLogService.filterAuditLogs(entityType, action));
    }
}