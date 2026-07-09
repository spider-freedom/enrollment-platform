package com.xju.enrollment.modules.system.dto;

public record LoginResponse(
        String token,
        Long userId,
        String name,
        String role,
        Long collegeId,
        String collegeName
) {
}
