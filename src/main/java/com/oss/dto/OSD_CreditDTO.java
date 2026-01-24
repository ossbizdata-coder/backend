package com.oss.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OSD_CreditDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String department;
    private Long shopId;
    private String shopName;
    private Double amount;
    private String reason;
    private Boolean isPaid;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;  // ⚠️ NEW: Timestamp for when credit was created
}