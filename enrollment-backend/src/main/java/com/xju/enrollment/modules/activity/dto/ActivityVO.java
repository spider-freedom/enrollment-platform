package com.xju.enrollment.modules.activity.dto;

import com.xju.enrollment.entity.Activity;

import java.time.LocalDateTime;

public record ActivityVO(
        Long id,
        String title,
        String description,
        String type,
        String status,
        Integer targetAudience,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime enrollStart,
        LocalDateTime enrollEnd,
        String location,
        Integer maxStudents,
        Integer maxTeachers,
        Integer maxPerSchool,
        String bannerUrl,
        String bannerLink,
        String groupRule,
        String rankRule,
        String workflowKey,
        Long creatorId,
        String creatorName,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
    public static ActivityVO from(Activity activity) {
        return new ActivityVO(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getType(),
                activity.getStatus(),
                activity.getTargetAudience(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getEnrollStart(),
                activity.getEnrollEnd(),
                activity.getLocation(),
                activity.getMaxStudents(),
                activity.getMaxTeachers(),
                activity.getMaxPerSchool(),
                activity.getBannerUrl(),
                activity.getBannerLink(),
                activity.getGroupRule(),
                activity.getRankRule(),
                activity.getWorkflowKey(),
                activity.getCreatorId(),
                null,
                activity.getCreateTime(),
                activity.getUpdateTime()
        );
    }

    public static ActivityVO from(Activity activity, String creatorName) {
        return new ActivityVO(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getType(),
                activity.getStatus(),
                activity.getTargetAudience(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getEnrollStart(),
                activity.getEnrollEnd(),
                activity.getLocation(),
                activity.getMaxStudents(),
                activity.getMaxTeachers(),
                activity.getMaxPerSchool(),
                activity.getBannerUrl(),
                activity.getBannerLink(),
                activity.getGroupRule(),
                activity.getRankRule(),
                activity.getWorkflowKey(),
                activity.getCreatorId(),
                creatorName,
                activity.getCreateTime(),
                activity.getUpdateTime()
        );
    }
}
