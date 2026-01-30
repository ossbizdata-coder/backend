package com.oss.model;
import jakarta.persistence.*;
import lombok.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.WORKING;

    // ✅ SIMPLE FLAG: true = working (YES), false = not working (NO)
    @Column(name = "is_working", nullable = false)
    @Builder.Default
    private Boolean isWorking = true;

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
}
