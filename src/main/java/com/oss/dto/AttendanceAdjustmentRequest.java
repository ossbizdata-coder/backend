package com.oss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAdjustmentRequest {
    private Double overtimeHours = 0.0;
    private Double deductionHours = 0.0;
    private String overtimeReason;
    private String deductionReason;
}

