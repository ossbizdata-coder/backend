package com.oss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCashSummaryDTO {
    private Long dailyCashId;
    private Long shopId;
    private String shopCode;
    private String shopName;
    private LocalDate businessDate;
    private Double openingCash;
    private Boolean openingConfirmed;
    private Double closingCash;
    private Boolean locked;
    private String closedByName;

    // Derived totals
    private Double totalExpenses;
    private Double manualSales;
    private Double totalCredits;
    private Double totalSales; // calculated: closing - opening + expenses - manual_sales
    private Double variance;

    // Transactions
    private List<CashTransactionDTO> expenses;
    private List<CashTransactionDTO> sales;
    private List<OSD_CreditDTO> credits; // NEW: Credits for this shop on this date
}

