package com.oss.repository;

import com.oss.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.entityType = :entityType AND a.entityId = :entityId
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> findByEntityTypeAndEntityId(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId
    );

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.user.id = :userId
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> findByUserId(@Param("userId") Long userId);
}

