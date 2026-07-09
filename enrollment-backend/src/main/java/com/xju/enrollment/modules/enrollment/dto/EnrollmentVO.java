package com.xju.enrollment.modules.enrollment.dto;

import com.xju.enrollment.entity.Enrollment;

import java.time.LocalDateTime;

public record EnrollmentVO(
        Long id,
        Long activityId,
        String activityTitle,
        Long userId,
        String userName,
        String userRole,
        String status,
        String targetSchool,
        String groupName,
        Integer rankInGroup,
        String rejectReason,
        Long collegeId,
        String collegeName,
        LocalDateTime submittedAt,
        LocalDateTime createTime
) {

    public static EnrollmentVO from(Enrollment e) {
        return new EnrollmentVO(
                e.getId(),
                e.getActivityId(),
                null,
                e.getUserId(),
                null,
                null,
                e.getStatus(),
                e.getTargetSchool(),
                e.getGroupName(),
                e.getRankInGroup(),
                e.getRejectReason(),
                e.getCollegeId(),
                e.getCollegeName(),
                e.getSubmittedAt(),
                e.getCreateTime()
        );
    }
}
