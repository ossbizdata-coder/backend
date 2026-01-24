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
public class MonthlyExpenseReportDTO {
    private Integer year;
    private Integer month;
    private String monthName;
    private Double totalExpenses;
    private List<DailyExpenseSummaryDTO> dailyExpenses;
    private List<ShopExpenseDTO> shopExpenses;
    private List<CategoryExpenseDTO> categoryExpenses;
}