package com.xju.enrollment.modules.feedback.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.common.ExcelExportUtil;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.modules.feedback.dto.FeedbackQuery;
import com.xju.enrollment.modules.feedback.dto.FeedbackReplyRequest;
import com.xju.enrollment.modules.feedback.dto.FeedbackSubmitRequest;
import com.xju.enrollment.modules.feedback.dto.FeedbackVO;
import com.xju.enrollment.modules.feedback.service.FeedbackService;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/student")
    public ApiResponse<FeedbackVO> submitStudent(@Valid @RequestBody FeedbackSubmitRequest request) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        FeedbackVO vo = feedbackService.submitStudent(userId, request);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/teacher")
    public ApiResponse<FeedbackVO> submitTeacher(@Valid @RequestBody FeedbackSubmitRequest request) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        FeedbackVO vo = feedbackService.submitTeacher(userId, request);
        return ApiResponse.ok(vo);
    }

    @GetMapping("/college")
    public ApiResponse<PageResult<FeedbackVO>> listForCollege(@Valid FeedbackQuery query) {
        PageResult<FeedbackVO> result = feedbackService.listForCollege(query);
        return ApiResponse.ok(result);
    }

    @GetMapping("/school")
    public ApiResponse<PageResult<FeedbackVO>> listForSchool(@Valid FeedbackQuery query) {
        PageResult<FeedbackVO> result = feedbackService.listForSchool(query);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<FeedbackVO> getDetail(@PathVariable Long id) {
        FeedbackVO vo = feedbackService.getDetail(id);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<Void> reply(@PathVariable Long id,
                                   @Valid @RequestBody FeedbackReplyRequest request) {
        Long replyUserId = Long.parseLong(SecurityUtils.getCurrentUserId());
        feedbackService.reply(id, request, replyUserId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/export")
    public void export(FeedbackQuery query, HttpServletResponse response) throws IOException {
        query = new FeedbackQuery(query.activityId(), query.collegeId(), query.status(), 1, 10000);
        var result = feedbackService.listForSchool(query);
        var data = result.list().stream().map(f -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("userName", f.userName());
            row.put("userRole", f.userRole());
            row.put("college", f.college());
            row.put("content", f.content());
            row.put("rating", f.rating());
            row.put("status", f.status());
            row.put("reply", f.reply());
            row.put("createTime", f.createTime());
            return row;
        }).toList();
        ExcelExportUtil.export(response, "反馈数据导出.xlsx", data,
            new String[]{"提交人","角色","学院","内容","评分","状态","回复","时间"},
            new String[]{"userName","userRole","college","content","rating","status","reply","createTime"});
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<FeedbackVO>> listMy(@Valid FeedbackQuery query) {
        Long userId = Long.valueOf(SecurityUtils.getCurrentUserId());
        return ApiResponse.ok(feedbackService.listMy(userId, query));
    }
}
