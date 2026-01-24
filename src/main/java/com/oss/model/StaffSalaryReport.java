package com.oss.model;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class StaffSalaryReport {
    private Long userId;
    private String name;
    private long totalMinutes;
    private double totalHours;
    private double hourlyRate;
    private double totalSalary;
}