package com.oss.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Entity
@Table(name = "shop_transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private String category; // SALE / EXPENSE
    @Column(name = "item_name")
    private String itemName;
    @Column(name = "shop_type", nullable = false)
    private String shopType;
    @Column(name = "department")
    private String department; // CAFE, BOOKSHOP, FOODHUT, COMMON
    @Column(name = "comment")
    private String comment; // Optional comment for expenses
    @Column(name = "opening_balance")
    private Double openingBalance; // For sales entries
    @Column(name = "closing_balance")
    private Double closingBalance; // For sales entries
    @Column(name = "total_expenses")
    private Double totalExpenses; // For sales entries
    @ManyToOne
    @JoinColumn(name = "expense_type_id")
    private ExpenseType expenseType;
    @ManyToOne
    @JoinColumn(name = "recorded_by", nullable = false)
    private User user;
    @Column(name = "business_date", nullable = false)
    private Long businessDate; // Stored as epoch milliseconds in SQLite
    @Column(name = "transaction_time", nullable = false)
    private Instant transactionTime;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}