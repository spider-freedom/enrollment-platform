package com.xju.enrollment.modules.system.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.modules.system.dto.UserVO;
import com.xju.enrollment.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/school/users")
@RequiredArgsConstructor
public class SchoolUserController {

    private final UserService userService;

    @GetMapping("/list")
    public ApiResponse<List<UserVO>> listAll() {
        List<User> users = userService.listAll();
        return ApiResponse.ok(users.stream().map(UserVO::from).toList());
    }

    @PostMapping("/{id}/promote-college")
    public ApiResponse<Void> promoteToCollegeAdmin(@PathVariable Long id) {
        userService.promoteToRole(id, "COLLEGE_ADMIN");
        return ApiResponse.ok("已提升为学院管理员", null);
    }

    @PostMapping("/{id}/promote-school")
    public ApiResponse<Void> promoteToSchoolAdmin(@PathVariable Long id) {
        userService.promoteToRole(id, "SCHOOL_ADMIN");
        return ApiResponse.ok("已提升为学校管理员", null);
    }

    @PostMapping("/{id}/demote")
    public ApiResponse<Void> demoteToTeacher(@PathVariable Long id) {
        userService.demoteToTeacher(id);
        return ApiResponse.ok("已降为教师", null);
    }

    @PostMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.setUserStatus(id, body.get("status"));
        return ApiResponse.ok("状态已更新", null);
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        userService.resetPasswordByAdmin(id);
        return ApiResponse.ok("密码已重置为123456", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ApiResponse.ok("用户已禁用", null);
    }
}
