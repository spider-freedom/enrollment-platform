package com.xju.enrollment.modules.system.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.AuditLog;
import com.xju.enrollment.modules.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/list")
    public ApiResponse<PageResult<AuditLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module) {
        PageResult<AuditLog> result = auditLogService.queryLogs(page, size, action, module);
        return ApiResponse.ok(result);
    }
}
