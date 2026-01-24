package com.oss.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Entity
@Table(name = "cash_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "daily_cash_id", nullable = false)
    private DailyCash dailyCash;
    @Column(nullable = false, length = 20)
    private String type; // EXPENSE, SALE
    @Column(nullable = false)
    private Double amount;
    @ManyToOne
    @JoinColumn(name = "expense_type_id")
    private ExpenseType expenseType;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}