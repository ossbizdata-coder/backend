package com.oss.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
/**
 * Daily Summary - Pre-calculated daily metrics for performance optimization
 * Automatically generated when a day is closed
 */
@Entity
@Table(name = "daily_summaries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"shop_id", "business_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;
    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;
    // Cash flow metrics
    @Column(name = "opening_cash", nullable = false)
    private Double openingCash;
    @Column(name = "closing_cash")
    private Double closingCash;
    @Column(name = "cash_difference")
    private Double cashDifference; // closing - opening
    // Revenue calculations
    @Column(name = "total_revenue")
    private Double totalRevenue; // (closing - opening) + expenses
    @Column(name = "total_expenses")
    private Double totalExpenses;
    @Column(name = "total_credits")
    private Double totalCredits;
    @Column(name = "net_sales")
    private Double netSales; // revenue - credits
    @Column(name = "profit")
    private Double profit; // revenue - expenses
    // Transaction counts
    @Column(name = "expense_count")
    private Integer expenseCount;
    @Column(name = "credit_count")
    private Integer creditCount;
    @Column(name = "manual_sale_count")
    private Integer manualSaleCount;
    // Staff metrics (if applicable)
    @Column(name = "staff_count")
    private Integer staffCount; // number of staff who checked in
    @Column(name = "total_attendance_hours")
    private Double totalAttendanceHours;
    // Status
    @Column(name = "is_closed")
    private Boolean isClosed;
    @Column(name = "closed_by_id")
    private Long closedById;
    @Column(name = "closed_at")
    private Long closedAt; // timestamp in milliseconds
    @Column(name = "calculated_at")
    private Long calculatedAt; // when this summary was calculated
    // User who closed the day
    @ManyToOne
    @JoinColumn(name = "closed_by_user_id")
    private User closedByUser;
}