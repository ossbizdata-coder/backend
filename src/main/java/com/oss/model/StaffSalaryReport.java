package com.oss.model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StaffSalaryReport {
    private Long userId;
    private String name;
    private long workDays;           // Count of days worked (isWorking = true)
    private double totalOvertimeHours;
    private double totalDeductionHours;
    private double dailyRate;
    private double totalSalary;      // workDays * dailyRate + overtime - deductions
}