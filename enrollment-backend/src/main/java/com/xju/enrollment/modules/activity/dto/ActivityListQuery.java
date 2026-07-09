package com.xju.enrollment.modules.activity.dto;

public record ActivityListQuery(
        String keyword,
        String type,
        String status,
        Integer page,
        Integer size
) {
    public ActivityListQuery {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 20;
        }
    }
}
