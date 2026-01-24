package com.oss.model;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class StaffAttendanceReport {
    private Long userId;
    private String name;
    private String email;
    private Long totalDays;
    private Long checkedInDays;
    private Long completedDays;
}