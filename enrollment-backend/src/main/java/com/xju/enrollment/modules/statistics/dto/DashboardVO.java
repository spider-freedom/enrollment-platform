package com.xju.enrollment.modules.statistics.dto;

public record DashboardVO(
        long totalActivities,
        long totalEnrollments,
        double approvalRate,
        double feedbackRate,
        double avgRating
) {}
