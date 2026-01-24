package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDataDTO {
    private Double totalRevenue;
    private Double totalExpenses;
    private Double netProfit;
    private Double profitMargin;
}