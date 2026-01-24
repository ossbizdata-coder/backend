package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopShopDTO {
    private Long shopId;
    private String shopCode;
    private String shopName;
    private Double revenue;
    private Double expenses;
    private Double profit;
}