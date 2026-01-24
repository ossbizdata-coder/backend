package com.oss.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for returning latest closing balance
 * Used by the optimized endpoint to reduce API calls
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestBalanceDTO {
    private Double closingBalance;
    private LocalDate date;
    private Long shopId;
}

