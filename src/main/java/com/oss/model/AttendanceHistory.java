package com.oss.model;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AttendanceHistory {
    private Long id; // <-- add id so history responses include record id
    private LocalDate workDate;
    private AttendanceStatus status;
    private Boolean isWorking;         // ✅ Simple flag for YES/NO
    private Double overtimeHours;
    private Double deductionHours;
    private String overtimeReason;
    private String deductionReason;

    // Custom JSON serialization for status - returns string instead of enum
    @JsonGetter("status")
    public String getStatusAsString() {
        return status != null ? status.name() : null;
    }
}