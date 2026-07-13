package com.xju.enrollment.modules.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ActivityRequest(
        @NotBlank(message = "活动标题不能为空")
        String title,

        String description,

        @NotBlank(message = "活动类型不能为空")
        String type,

        String category,

        String status,

        @NotNull(message = "目标受众不能为空")
        Integer targetAudience,

        LocalDateTime startTime,

        LocalDateTime endTime,

        LocalDateTime enrollStart,

        LocalDateTime enrollEnd,

        String location,

        Integer maxStudents,

        Integer maxTeachers,

        Integer maxPerSchool,

        Long collegeId,

        String collegeName,

        String bannerUrl,

        String bannerLink,

        String groupRule,

        String rankRule,

        String workflowKey
) {
}
