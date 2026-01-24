package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalaryDTO {
    private Double dailySalary;
    private Double deductionRatePerHour;
    private Integer totalDaysWorked;
    private Double totalSalary;
    private List<DailySalaryBreakdown> dailyBreakdown;
    private Integer year;
    private Integer month;
    private String userName;
    private String userEmail;
}