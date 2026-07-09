package com.xju.enrollment.modules.system.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        String password,

        @NotBlank(message = "姓名不能为空")
        String name,

        @NotBlank(message = "角色不能为空")
        String role,

        Long collegeId,

        String collegeName,

        String major,

        String grade,

        String email,

        String phone
) {
}
