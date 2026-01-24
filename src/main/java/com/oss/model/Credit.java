package com.oss.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credits")
@Data
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Links to your existing User table

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shop_id")
    private Shop shop; // NEW: Link to shop for daily credits per shop

    @Column
    private String department; // CAFE, BOOKSHOP, FOODHUT, COMMON (kept for backward compatibility)

    @Column(nullable = false)
    private Double amount;

    private String reason;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "transaction_date")
    private LocalDate transactionDate; // Business date when credit was given

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
