package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopExpenseCategoryDTO {
    private Long expenseTypeId;
    private String expenseTypeName;
    private Double amount;
}