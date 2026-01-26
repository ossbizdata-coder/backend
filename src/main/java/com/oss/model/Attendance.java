package com.oss.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    @Column(name = "check_in_time")
    private Instant checkInTime;
    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "total_minutes")
    private Long totalMinutes;

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(Long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    private Double latitude;
    private Double longitude;
    @Column(nullable = false)
    @Builder.Default
    private boolean manualCheckout = false;
    @Column(name = "overtime_hours")
    @Builder.Default
    private Double overtimeHours = 0.0;
    @Column(name = "deduction_hours")
    @Builder.Default
    private Double deductionHours = 0.0;
    @Column(name = "overtime_reason", columnDefinition = "TEXT")
    private String overtimeReason;
    @Column(name = "deduction_reason", columnDefinition = "TEXT")
    private String deductionReason;
    public long getWorkedMinutes() {
        if (checkInTime == null || checkOutTime == null) return 0;
        return Duration.between(checkInTime, checkOutTime).toMinutes();
    }
}
