package com.oss.model;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AttendanceHistory {
    private LocalDate workDate;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long totalMinutes;
    private Boolean manualCheckout;
    private AttendanceStatus status;

    // Custom JSON serialization for status - returns string instead of enum
    @JsonGetter("status")
    public String getStatusAsString() {
        return status != null ? status.name() : null;
    }
}