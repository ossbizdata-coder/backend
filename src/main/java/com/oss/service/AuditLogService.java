package com.oss.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oss.model.AuditLog;
import com.oss.model.User;
import com.oss.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepo;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepo) {
        this.auditLogRepo = auditLogRepo;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create an audit log entry
     */
    public void createAuditLog(User user, String action, String entityType, Long entityId,
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

    /**
     * Get all audit logs
     */
    public List<Map<String, Object>> getAllAuditLogs() {
        return auditLogRepo.findAll().stream()
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs for a specific entity
     */
    public List<Map<String, Object>> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepo.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by user
     */
    public List<Map<String, Object>> getAuditLogsByUser(Long userId) {
        return auditLogRepo.findByUserId(userId).stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * Filter audit logs
     */
    public List<Map<String, Object>> filterAuditLogs(String entityType, String action) {
        return auditLogRepo.findAll().stream()
                .filter(log -> entityType == null || log.getEntityType().equals(entityType))
                .filter(log -> action == null || log.getAction().equals(action))
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * Convert AuditLog to Map for response
     */
    private Map<String, Object> convertToMap(AuditLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("userId", log.getUser() != null ? log.getUser().getId() : null);
        map.put("userName", log.getUser() != null ? log.getUser().getName() : null);
        map.put("userEmail", log.getUser() != null ? log.getUser().getEmail() : null);
        map.put("action", log.getAction());
        map.put("entityType", log.getEntityType());
        map.put("entityId", log.getEntityId());
        map.put("oldValue", parseJson(log.getOldValue()));
        map.put("newValue", parseJson(log.getNewValue()));
        map.put("createdAt", log.getCreatedAt().toString());
        return map;
    }

    /**
     * Parse JSON string to Map
     */
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}

