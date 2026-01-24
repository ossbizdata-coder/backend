package com.oss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oss.model.Transaction;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
public class OSD_TransactionResponse {
    private Long id;
    private Double amount;
    private String category;

    @JsonProperty("item")  // Mobile app expects "item" not "itemName"
    private String itemName;

    @JsonProperty("shopType")  // Mobile app expects camelCase "shopType"
    private String shopType;

    private String department;
    private String comment;
    private Double openingBalance;
    private Double closingBalance;
    private Double totalExpenses;

    // Expense type fields (flattened)
    private Long expenseTypeId;
    private String expenseTypeName;
    private String expenseTypeShopType;

    // User info
    private Long recordedBy;
    private String recordedByName;

    // Date/time fields
    private Long businessDate; // Stored as epoch milliseconds

    @JsonProperty("transactionDate")  // Mobile app uses this for date filtering
    private String transactionDate; // ISO format: "2026-01-08T00:00:00"

    private Instant transactionTime;
    private Instant createdAt;

    // Permission flags (to be set by controller based on user role)
    private Boolean canEdit;
    private Boolean canDelete;

    public static OSD_TransactionResponse from(Transaction transaction) {
        OSD_TransactionResponse dto = new OSD_TransactionResponse();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setCategory(transaction.getCategory());
        dto.setItemName(transaction.getItemName());
        dto.setShopType(transaction.getShopType());
        dto.setDepartment(transaction.getDepartment());
        dto.setComment(transaction.getComment());
        dto.setOpeningBalance(transaction.getOpeningBalance());
        dto.setClosingBalance(transaction.getClosingBalance());
        dto.setTotalExpenses(transaction.getTotalExpenses());

        // Flatten expense type
        if (transaction.getExpenseType() != null) {
            dto.setExpenseTypeId(transaction.getExpenseType().getId());
            dto.setExpenseTypeName(transaction.getExpenseType().getName());
            dto.setExpenseTypeShopType(transaction.getExpenseType().getShopType());
        }

        // User info
        if (transaction.getUser() != null) {
            dto.setRecordedBy(transaction.getUser().getId());
            dto.setRecordedByName(transaction.getUser().getName());
        }

        // Date/time - CRITICAL: Convert businessDate to transactionDate ISO string
        dto.setBusinessDate(transaction.getBusinessDate());

        // Convert epoch milliseconds to ISO 8601 date string with Z suffix
        // Format: "2026-01-08T00:00:00Z" for mobile app filtering
        if (transaction.getBusinessDate() != null) {
            dto.setTransactionDate(
                Instant.ofEpochMilli(transaction.getBusinessDate())
                    .atZone(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_INSTANT)
            );
        }

        dto.setTransactionTime(transaction.getTransactionTime());
        dto.setCreatedAt(transaction.getCreatedAt());

        return dto;
    }
}

