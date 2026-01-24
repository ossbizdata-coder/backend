package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendsDTO {
    private Double revenueGrowth;
    private Double expenseGrowth;
    private Double profitGrowth;
}