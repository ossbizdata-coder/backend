package com.oss.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Entity
@Table(name = "improvement")
@Data
public class Improvement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private Instant createdAt = Instant.now();
}