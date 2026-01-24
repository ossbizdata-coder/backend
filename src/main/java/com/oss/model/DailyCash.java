package com.oss.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "daily_cash", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"shop_id", "business_date"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCash {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;
    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;
    @Column(name = "opening_cash", nullable = false)
    private Double openingCash;
    @Column(name = "opening_confirmed", nullable = false)
    @Builder.Default
    private Boolean openingConfirmed = false;
    @Column(name = "closing_cash")
    private Double closingCash;
    @Column(nullable = false)
    @Builder.Default
    private Boolean locked = false;
    @ManyToOne
    @JoinColumn(name = "closed_by_id")
    private User closedBy;
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}