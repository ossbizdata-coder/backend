package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopExpenseDetailDTO {
    private Long shopId;
    private String shopCode;
    private String shopName;
    private Double totalExpenses;
    private Double percentage;
    private Long expenseCount;
    private Double averageExpense;
    private TopExpenseCategoryDTO topExpenseCategory;
}