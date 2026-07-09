package com.xju.enrollment.modules.workflow.dto;

import java.util.List;

public record BatchApprovalRequest(
        List<Long> enrollmentIds,
        String action,
        String comment
) {
}
