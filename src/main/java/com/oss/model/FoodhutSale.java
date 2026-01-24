package com.oss.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "foodhut_sales")
public class FoodhutSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variation_id", nullable = false)
    private FoodhutItemVariation itemVariation;
    @Column(name = "prepared_qty", nullable = false)
    private int preparedQty;
    @Column(name = "remaining_qty", nullable = false)
    private int remainingQty;
    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;
    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType = "PREPARED";
    // No-args constructor (needed by JPA)
    public FoodhutSale() {}
    // All-args constructor
    public FoodhutSale(FoodhutItemVariation itemVariation, int preparedQty, int remainingQty, LocalDateTime transactionTime, User recordedBy, String actionType) {
        this.itemVariation = itemVariation;
        this.preparedQty = preparedQty;
        this.remainingQty = remainingQty;
        this.transactionTime = transactionTime;
        this.recordedBy = recordedBy;
        this.actionType = actionType;
    }
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FoodhutItemVariation getItemVariation() { return itemVariation; }
    public void setItemVariation(FoodhutItemVariation itemVariation) { this.itemVariation = itemVariation; }
    public int getPreparedQty() { return preparedQty; }
    public void setPreparedQty(int preparedQty) { this.preparedQty = preparedQty; }
    public int getRemainingQty() { return remainingQty; }
    public void setRemainingQty(int remainingQty) { this.remainingQty = remainingQty; }
    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }
    public User getRecordedBy() { return recordedBy; }
    public void setRecordedBy(User recordedBy) { this.recordedBy = recordedBy; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
}