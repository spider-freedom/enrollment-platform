package com.xju.enrollment.modules.feedback.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record FeedbackReplyRequest(
        @NotBlank(message = "回复内容不能为空")
        String replyContent,

        List<Long> attachmentIds
) {}
