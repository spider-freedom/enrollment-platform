package com.xju.enrollment.modules.system.dto;

import com.xju.enrollment.entity.User;

public record UserVO(
        Long id,
        String username,
        String name,
        String role,
        Long collegeId,
        String collegeName,
        String major,
        String grade,
        Double gpa,
        String email,
        String phone,
        String avatar,
        String status,
        String createTime
) {
    public static UserVO from(User user) {
        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole(),
                user.getCollegeId(),
                user.getCollegeName(),
                user.getMajor(),
                user.getGrade(),
                user.getGpa(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                user.getCreateTime() != null ? user.getCreateTime().toString() : null
        );
    }
}
