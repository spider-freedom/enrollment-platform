package com.xju.enrollment.modules.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApprovalRequest(
        @NotNull Long enrollmentId,
        @NotBlank String action,
        String comment,
        String rejectReason
) {
}
