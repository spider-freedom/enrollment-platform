package com.xju.enrollment.modules.activity.dto;

import com.xju.enrollment.entity.Activity;

import java.time.LocalDateTime;

public record ActivityVO(
        Long id,
        String title,
        String description,
        String type,
        String category,
        String level,
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
        Integer currentStudents,
        Integer currentTeachers,
        String bannerUrl,
        String bannerLink,
        String groupRule,
        String rankRule,
        String workflowKey,
        Long creatorId,
        String creatorName,
        Long collegeId,
        String collegeName,
        Integer isBanner,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
    public static ActivityVO from(Activity activity) {
        return from(activity, null);
    }

    public static ActivityVO from(Activity activity, String creatorName) {
        return from(activity, creatorName, 0, 0);
    }

    public static ActivityVO from(Activity activity, String creatorName,
                                  Integer currentStudents, Integer currentTeachers) {
        return new ActivityVO(
                activity.getId(), activity.getTitle(), activity.getDescription(),
                activity.getType(), activity.getCategory(), activity.getLevel(), activity.getStatus(), activity.getTargetAudience(),
                activity.getStartTime(), activity.getEndTime(),
                activity.getEnrollStart(), activity.getEnrollEnd(),
                activity.getLocation(), activity.getMaxStudents(), activity.getMaxTeachers(),
                activity.getMaxPerSchool(),
                currentStudents != null ? currentStudents : 0,
                currentTeachers != null ? currentTeachers : 0,
                activity.getBannerUrl(), activity.getBannerLink(),
                activity.getGroupRule(), activity.getRankRule(), activity.getWorkflowKey(),
                activity.getCreatorId(), creatorName,
                activity.getCollegeId(), activity.getCollegeName(), activity.getIsBanner(),
                activity.getCreateTime(), activity.getUpdateTime()
        );
    }
}