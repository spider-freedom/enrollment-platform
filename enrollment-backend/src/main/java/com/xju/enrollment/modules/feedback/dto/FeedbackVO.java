package com.xju.enrollment.modules.feedback.dto;

import com.xju.enrollment.entity.Feedback;

import java.time.LocalDateTime;

public record FeedbackVO(
        Long feedbackId,
        Long activityId,
        String activityTitle,
        Long userId,
        String userName,
        String userRole,
        String college,
        String content,
        Integer rating,
        String status,
        String reply,
        String replyUserName,
        LocalDateTime replyTime,
        int attachmentCount,
        LocalDateTime createTime
) {

    public static FeedbackVO from(Feedback f) {
        return new FeedbackVO(
                f.getId(),
                f.getActivityId(),
                null,
                f.getUserId(),
                null,
                f.getUserRole(),
                null,
                f.getContent(),
                f.getRating(),
                f.getStatus(),
                f.getReply(),
                null,
                f.getReplyTime(),
                0,
                f.getCreateTime()
        );
    }
}