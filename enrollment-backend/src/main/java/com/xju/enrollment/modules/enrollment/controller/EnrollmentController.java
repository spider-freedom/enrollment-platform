package com.xju.enrollment.modules.enrollment.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.common.ExcelExportUtil;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.modules.enrollment.dto.EnrollmentQuery;
import com.xju.enrollment.modules.enrollment.dto.EnrollmentRequest;
import com.xju.enrollment.modules.enrollment.dto.EnrollmentVO;
import com.xju.enrollment.modules.enrollment.service.EnrollmentService;
import com.xju.enrollment.modules.system.service.UserService;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @PostMapping("/submit")
    public ApiResponse<EnrollmentVO> submit(@Valid @RequestBody EnrollmentRequest request) {
        Long userId = getCurrentUserIdAsLong();
        EnrollmentVO vo = enrollmentService.submit(request, userId);
        return ApiResponse.ok(vo);
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<EnrollmentVO>> listMy(EnrollmentQuery query) {
        Long userId = getCurrentUserIdAsLong();
        PageResult<EnrollmentVO> result = enrollmentService.listMy(userId, query);
        return ApiResponse.ok(result);
    }

    @GetMapping("/college")
    public ApiResponse<PageResult<EnrollmentVO>> listForCollege(EnrollmentQuery query) {
        Long userId = getCurrentUserIdAsLong();
        User user = userService.getById(userId);
        if (user.getCollegeId() == null) {
            throw new BusinessException("未关联学院");
        }
        PageResult<EnrollmentVO> result = enrollmentService.listForCollege(query, user.getCollegeId());
        return ApiResponse.ok(result);
    }

    @GetMapping("/school")
    public ApiResponse<PageResult<EnrollmentVO>> listForSchool(EnrollmentQuery query) {
        PageResult<EnrollmentVO> result = enrollmentService.listForSchool(query);
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdraw(@PathVariable Long id) {
        Long userId = getCurrentUserIdAsLong();
        enrollmentService.withdraw(id, userId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<EnrollmentVO> getById(@PathVariable Long id) {
        EnrollmentVO vo = enrollmentService.getById(id);
        return ApiResponse.ok(vo);
    }

    @GetMapping("/export")
    public void export(EnrollmentQuery query, HttpServletResponse response) throws IOException {
        query = new EnrollmentQuery(query.activityId(), query.status(), query.collegeId(), 1, 10000);
        var result = enrollmentService.listForSchool(query);
        // Build export data
        var data = result.list().stream().map(e -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("activityTitle", e.activityTitle());
            row.put("userName", e.userName());
            row.put("userRole", e.userRole());
            row.put("collegeName", e.collegeName());
            row.put("targetSchool", e.targetSchool());
            row.put("status", e.status());
            row.put("submittedAt", e.submittedAt());
            row.put("groupName", e.groupName());
            row.put("rankInGroup", e.rankInGroup());
            return row;
        }).toList();
        ExcelExportUtil.export(response, "报名数据导出.xlsx", data,
            new String[]{"活动名称","申请人","角色","学院","目标学校","状态","提交时间","分组","组内排名"},
            new String[]{"activityTitle","userName","userRole","collegeName","targetSchool","status","submittedAt","groupName","rankInGroup"});
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
