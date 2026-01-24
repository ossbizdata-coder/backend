package com.oss.dto;
import com.oss.model.CashTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashTransactionDTO {
    private Long id;
    private Long dailyCashId;
    private String type; // EXPENSE, SALE
    private Double amount;
    private Long expenseTypeId;
    private String expenseTypeName;
    private String description;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime createdAt;
    public static CashTransactionDTO from(CashTransaction ct) {
        return CashTransactionDTO.builder()
                .id(ct.getId())
                .dailyCashId(ct.getDailyCash().getId())
                .type(ct.getType())
                .amount(ct.getAmount())
                .expenseTypeId(ct.getExpenseType() != null ? ct.getExpenseType().getId() : null)
                .expenseTypeName(ct.getExpenseType() != null ? ct.getExpenseType().getName() : null)
                .description(ct.getDescription())
                .recordedById(ct.getRecordedBy().getId())
                .recordedByName(ct.getRecordedBy().getName())
                .createdAt(ct.getCreatedAt())
                .build();
    }
}