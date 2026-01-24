package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseDTO {
    private Long expenseTypeId;
    private String expenseTypeName;
    private String department;
    private Double totalAmount;
    private Long transactionCount;
    private Double averageAmount;
    private Double percentage;
}