package com.oss.dto;

import lombok.Data;

@Data
public class OSD_TransactionUpdateRequest {
    private Double amount;
    private String description; // Maps to itemName field
    private String comment;
    private Long expenseTypeId;
}

