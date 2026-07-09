package com.xju.enrollment.modules.workflow.dto;

import java.time.LocalDateTime;

public record ApprovalVO(
        Long enrollmentId,
        String applicantName,
        String applicantRole,
        String activityTitle,
        String targetSchool,
        Double gpa,
        String collegeName,
        String currentStatus,
        Boolean collegeApproved,
        String comment,
        LocalDateTime createTime
) {
}
