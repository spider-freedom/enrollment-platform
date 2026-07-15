package com.xju.enrollment.modules.workflow.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.modules.workflow.dto.ApprovalRequest;
import com.xju.enrollment.modules.workflow.dto.ApprovalVO;
import com.xju.enrollment.modules.workflow.dto.BatchApprovalRequest;
import com.xju.enrollment.modules.workflow.service.ApprovalService;
import com.xju.enrollment.modules.system.service.UserService;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final UserService userService;

    @GetMapping("/college")
    public ApiResponse<PageResult<ApprovalVO>> getPendingListForCollege(
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserIdAsLong();
        User user = userService.getById(userId);
        if (user.getCollegeId() == null) {
            throw new BusinessException("未关联学院");
        }
        if (!"COLLEGE_ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "仅学院管理员可访问");
        }
        PageResult<ApprovalVO> result = approvalService.getPendingListForCollege(
                user.getCollegeId(), activityId, status, page, size);
        return ApiResponse.ok(result);
    }

    @GetMapping("/school")
    public ApiResponse<PageResult<ApprovalVO>> getPendingListForSchool(
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserIdAsLong();
        User user = userService.getById(userId);
        if (!"SCHOOL_ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "仅校级管理员可访问");
        }
        PageResult<ApprovalVO> result = approvalService.getPendingListForSchool(
                collegeId, activityId, status, page, size);
        return ApiResponse.ok(result);
    }

    @PostMapping("/approve")
    public ApiResponse<Void> approve(@Valid @RequestBody ApprovalRequest request) {
        Long approverId = getCurrentUserIdAsLong();
        approvalService.approve(request.enrollmentId(), request, approverId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/reject")
    public ApiResponse<Void> reject(@Valid @RequestBody ApprovalRequest request) {
        approvalService.reject(request.enrollmentId(), request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch")
    public ApiResponse<Void> batchApprove(@Valid @RequestBody BatchApprovalRequest request) {
        Long approverId = getCurrentUserIdAsLong();
        approvalService.batchApprove(request, approverId);
        return ApiResponse.ok(null);
    }

    private Long getCurrentUserIdAsLong() {
        String userIdStr = SecurityUtils.getCurrentUserId();
        if (userIdStr == null) {
            throw new BusinessException(401, "未登录");
        }
        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "无效的用户身份");
        }
    }
}
