package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSummaryDTO {
    private SummaryDataDTO summary;
    private PeriodDataDTO currentMonth;
    private PeriodDataDTO previousMonth;
    private TopShopDTO topPerformingShop;
    private TrendsDTO recentTrends;
}