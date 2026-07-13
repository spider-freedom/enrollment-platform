package com.xju.enrollment.modules.system.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.modules.system.dto.ChangePasswordRequest;
import com.xju.enrollment.modules.system.dto.UserVO;
import com.xju.enrollment.modules.system.service.UserService;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        try {
            String uploadDir = "./uploads/avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String ext = ".png";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf("."));
            String fileName = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
            Files.write(Paths.get(uploadDir, fileName), file.getBytes());
            String avatarUrl = "/uploads/avatars/" + fileName;
            userService.updateProfile(userId, new UserVO(null, null, null, null, null, null, null, null, null, null, null, avatarUrl, null, null));
            Map<String, String> result = new HashMap<>();
            result.put("url", avatarUrl);
            return ApiResponse.ok(result);
        } catch (IOException e) {
            return ApiResponse.fail("头像上传失败");
        }
    }
}
