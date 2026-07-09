package com.xju.enrollment.modules.enrollment.dto;

public record EnrollmentQuery(
        Long activityId,
        String status,
        Long collegeId,
        Integer page,
        Integer size
) {

    public EnrollmentQuery {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 20;
        }
    }
}
