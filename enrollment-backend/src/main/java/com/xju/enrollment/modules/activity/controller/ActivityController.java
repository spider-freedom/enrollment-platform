package com.xju.enrollment.modules.activity.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.common.ExcelExportUtil;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.modules.activity.dto.ActivityVO;
import java.util.List;
import com.xju.enrollment.modules.activity.dto.ActivityListQuery;
import com.xju.enrollment.modules.activity.dto.ActivityRequest;
import com.xju.enrollment.modules.activity.service.ActivityService;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/create")
    public ApiResponse<ActivityVO> createActivity(@Valid @RequestBody ActivityRequest request) {
        Long creatorId = Long.valueOf(SecurityUtils.getCurrentUserId());
        ActivityVO activity = activityService.createActivity(request, creatorId);
        return ApiResponse.ok(activity);
    }

    @PostMapping("/{id}/banner")
    public ApiResponse<Void> toggleBanner(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        activityService.toggleBanner(id, body.get("isBanner"));
        return ApiResponse.ok(null);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Void> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        activityService.updateActivity(id, request);
        return ApiResponse.ok("更新成功", null);
    }

    @GetMapping("/list/student")
    public ApiResponse<PageResult<ActivityVO>> listForStudent(ActivityListQuery query) {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        PageResult<ActivityVO> result = activityService.listForStudent(query, userId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/list/teacher")
    public ApiResponse<PageResult<ActivityVO>> listForTeacher(ActivityListQuery query) {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        PageResult<ActivityVO> result = activityService.listForTeacher(query, userId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/list/college")
    public ApiResponse<PageResult<ActivityVO>> listForCollege(ActivityListQuery query) {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        PageResult<ActivityVO> result = activityService.listForCollege(query, userId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/list/school")
    public ApiResponse<PageResult<ActivityVO>> listForSchool(ActivityListQuery query) {
        PageResult<ActivityVO> result = activityService.listForSchool(query);
        return ApiResponse.ok(result);
    }

        @GetMapping("/banners")
    public ApiResponse<List<ActivityVO>> getBanners() {
        return ApiResponse.ok(activityService.getBanners());
    }

    @GetMapping("/export")
    public void export(ActivityListQuery query, HttpServletResponse response) throws IOException {
        query = new ActivityListQuery(query.keyword(), query.type(), query.category(), query.status(), 1, 10000);
        var result = activityService.listForSchool(query);
        var data = result.list().stream().map(a -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("title", a.title()); row.put("type", a.type()); row.put("status", a.status());
            row.put("startTime", a.startTime()); row.put("endTime", a.endTime());
            row.put("location", a.location());
            return row;
        }).toList();
        ExcelExportUtil.export(response, "活动数据导出.xlsx", data,
            new String[]{"活动名称","类型","状态","开始时间","结束时间","地点"},
            new String[]{"title","type","status","startTime","endTime","location"});
    }

    @GetMapping("/{id}")
    public ApiResponse<ActivityVO> getById(@PathVariable Long id) {
        ActivityVO activity = activityService.getById(id);
        return ApiResponse.ok(activity);
    }
}
