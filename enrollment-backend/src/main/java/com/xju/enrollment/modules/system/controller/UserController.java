package com.xju.enrollment.modules.system.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.modules.system.dto.ChangePasswordRequest;
import com.xju.enrollment.modules.system.dto.UserVO;
import com.xju.enrollment.modules.system.service.UserService;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<UserVO> getProfile() {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        UserVO profile = userService.getProfile(userId);
        return ApiResponse.ok(profile);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UserVO vo) {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        userService.updateProfile(userId, vo);
        return ApiResponse.ok("更新成功", null);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        userService.changePassword(userId, request);
        return ApiResponse.ok("密码修改成功", null);
    }
}
