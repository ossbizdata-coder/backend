package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalaryBreakdown {
    private String date;
    private Double hours;
    private Double salary;
    private Double overtimeHours;
    private Double deductionHours;
    private String overtimeReason;
    private String deductionReason;
    private Boolean qualifiedForFullSalary;
}