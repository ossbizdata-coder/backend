package com.oss.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSS_DailySalaryDto {
    private LocalDate date;
    private double hours;
    private double salary;
    private Double overtimeHours;
    private Double deductionHours;
    private String overtimeReason;
    private String deductionReason;
    private Boolean qualified;

    // Constructor for backward compatibility
    public OSS_DailySalaryDto(LocalDate date, double hours, double salary) {
        this.date = date;
        this.hours = hours;
        this.salary = salary;
        this.overtimeHours = 0.0;
        this.deductionHours = 0.0;
        this.qualified = hours >= 6.0;
    }
}
