package com.oss.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;
@Data
@AllArgsConstructor
public class AttendanceHistory {
    private Instant workDate;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long totalMinutes;
    private boolean manualCheckout;
    private String status;
}