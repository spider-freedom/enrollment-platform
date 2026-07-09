package com.xju.enrollment.modules.feedback.dto;

public record FeedbackQuery(
        Long activityId,
        Long collegeId,
        String status,
        Integer page,
        Integer size
) {
    public FeedbackQuery {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 20;
        }
    }
}
