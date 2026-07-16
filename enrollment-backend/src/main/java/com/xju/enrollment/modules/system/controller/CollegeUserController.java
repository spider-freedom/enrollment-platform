package com.xju.enrollment.modules.system.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.modules.system.dto.UserVO;
import com.xju.enrollment.modules.system.service.UserService;
import com.xju.enrollment.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/college/users")
@RequiredArgsConstructor
public class CollegeUserController {

    private final UserService userService;

    @GetMapping("/list")
    public ApiResponse<List<UserVO>> listByCollege() {
        Long collegeId = getCurrentUserCollegeId();
        List<User> users = userService.listByCollege(collegeId);
        List<UserVO> vos = users.stream().map(UserVO::from).toList();
        return ApiResponse.ok(vos);
    }

    @GetMapping("/admins")
    public ApiResponse<List<UserVO>> listCollegeAdmins() {
        Long collegeId = getCurrentUserCollegeId();
        List<User> users = userService.listCollegeAdmins(collegeId);
        List<UserVO> vos = users.stream().map(UserVO::from).toList();
        return ApiResponse.ok(vos);
    }

    @PostMapping("/{id}/promote")
    public ApiResponse<Void> promoteToAdmin(@PathVariable Long id) {
        Long collegeId = getCurrentUserCollegeId();
        userService.promoteToAdmin(id, collegeId);
        return ApiResponse.ok("已提升为学院管理员", null);
    }

    @PostMapping("/{id}/demote")
    public ApiResponse<Void> demoteAdmin(@PathVariable Long id) {
        Long collegeId = getCurrentUserCollegeId();
        userService.demoteAdmin(id, collegeId);
        return ApiResponse.ok("已降级为教师", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        Long collegeId = getCurrentUserCollegeId();
        User user = userService.getById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if (!collegeId.equals(user.getCollegeId())) throw new BusinessException("无权操作该用户");
        userService.hardDeleteUser(id);
        return ApiResponse.ok("用户已删除", null);
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        Long operatorId = Long.valueOf(SecurityUtils.getCurrentUserId());
        userService.resetPassword(id, operatorId);
        return ApiResponse.ok("密码已重置为123456", null);
    }

    @PostMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long operatorId = Long.valueOf(SecurityUtils.getCurrentUserId());
        String status = body.get("status");
        userService.toggleUserStatus(id, status, operatorId);
        return ApiResponse.ok("状态已更新", null);
    }

    @PostMapping("/import")
    public ApiResponse<Map<String, Integer>> importUsers(@RequestParam("file") MultipartFile file) {
        Long operatorId = Long.valueOf(SecurityUtils.getCurrentUserId());
        Map<String, Integer> result = userService.importUsers(file, operatorId);
        return ApiResponse.ok("导入完成", result);
    }

    private Long getCurrentUserCollegeId() {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        User currentUser = userService.getById(userId);
        return currentUser.getCollegeId();
    }
}
