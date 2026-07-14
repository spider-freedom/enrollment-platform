package com.xju.enrollment.modules.enrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull Long activityId,
        String targetSchool,
        String intro,
        String contact
) {
}
