package com.oss.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseByShopReportDTO {
    private String startDate;
    private String endDate;
    private Double totalExpenses;
    private List<ShopExpenseDetailDTO> shops;
}