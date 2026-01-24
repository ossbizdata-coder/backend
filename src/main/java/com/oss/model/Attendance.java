package com.oss.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant workDate;

    private Instant checkInTime;
    private Instant checkOutTime;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private String status;

    private Long totalMinutes;

    @Column(nullable = false)
    private boolean manualCheckout;

    @Column(name = "overtime_hours")
    @Builder.Default
    private Double overtimeHours = 0.0;

    @Column(name = "deduction_hours")
    @Builder.Default
    private Double deductionHours = 0.0;

    @Column(name = "overtime_reason")
    private String overtimeReason;

    @Column(name = "deduction_reason")
    private String deductionReason;

    public long getWorkedMinutes() {
        if (checkInTime == null || checkOutTime == null) return 0;
        return Duration.between(checkInTime, checkOutTime).toMinutes();
    }
}
