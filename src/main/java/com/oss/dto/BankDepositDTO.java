package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDepositDTO {
    private Long transactionId;
    private Double amount;
    private Long timestamp;
    private String description;
    private String shop;
    private String shopName;
    private Long expenseTypeId;
    private String expenseTypeName;
    private String createdBy;
}