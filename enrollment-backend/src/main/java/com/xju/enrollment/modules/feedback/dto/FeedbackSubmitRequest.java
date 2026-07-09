package com.xju.enrollment.modules.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackSubmitRequest(
        @NotNull(message = "活动ID不能为空")
        Long activityId,

        @NotBlank(message = "反馈内容不能为空")
        String content,

        @Min(value = 1, message = "评分最小为1")
        @Max(value = 5, message = "评分最大为5")
        Integer rating,

        String contact,

        String department
) {}
