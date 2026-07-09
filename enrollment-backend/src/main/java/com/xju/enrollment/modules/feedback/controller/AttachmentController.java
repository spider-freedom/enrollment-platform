package com.xju.enrollment.modules.feedback.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.entity.FeedbackAttachment;
import com.xju.enrollment.modules.feedback.service.AttachmentService;
import com.xju.enrollment.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "feedbackId", required = false) Long feedbackId) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        FeedbackAttachment attachment = attachmentService.upload(file, feedbackId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("attachmentId", attachment.getId());
        result.put("fileName", attachment.getFileName());
        result.put("fileSize", attachment.getFileSize());
        result.put("fileType", attachment.getFileType());
        return ApiResponse.ok(result);
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        attachmentService.download(id, response);
    }
}
