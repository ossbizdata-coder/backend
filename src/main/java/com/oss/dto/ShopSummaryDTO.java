package com.oss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopSummaryDTO {
    private Long shopId;
    private String shopCode;
    private String shopName;
    private Double latestClosingCash;
    private String lastUpdatedDate;
}

