package com.oss.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String action; // EDIT, DELETE
    @Column(name = "entity_type", nullable = false)
    private String entityType; // TRANSACTION, EXPENSE, etc.
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // JSON representation of old data
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // JSON representation of new data
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}