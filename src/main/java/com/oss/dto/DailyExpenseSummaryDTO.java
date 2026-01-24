package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyExpenseSummaryDTO {
    private String date;
    private Double totalExpenses;
    private Long expenseCount;
}