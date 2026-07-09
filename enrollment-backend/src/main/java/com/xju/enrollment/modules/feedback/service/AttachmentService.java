package com.xju.enrollment.modules.feedback.service;

import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.entity.Feedback;
import com.xju.enrollment.entity.FeedbackAttachment;
import com.xju.enrollment.mapper.FeedbackAttachmentMapper;
import com.xju.enrollment.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final FeedbackAttachmentMapper feedbackAttachmentMapper;
    private final FeedbackMapper feedbackMapper;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.allowed-extensions:pdf,doc,docx,xls,xlsx,jpg,jpeg,png,gif,mp4}")
    private String allowedExtensionsStr;

    @Value("${file.max-size:10485760}")
    private long maxFileSize;

    public FeedbackAttachment upload(MultipartFile file, Long feedbackId, Long userId) {
        if (feedbackId != null) {
            Feedback feedback = feedbackMapper.selectById(feedbackId);
            if (feedback == null) {
                throw new BusinessException("反馈不存在");
            }
        }

        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制，最大 " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("文件格式不支持");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedExtensionsStr.split(","));
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException("不支持的文件格式: " + extension + "，允许的格式: " + allowedExtensionsStr);
        }

        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        Path uploadDir = Paths.get(uploadPath);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new BusinessException("创建上传目录失败: " + e.getMessage());
        }

        Path targetPath = uploadDir.resolve(storedFilename);
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }

        FeedbackAttachment attachment = new FeedbackAttachment();
        attachment.setFeedbackId(feedbackId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(targetPath.toString());
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        attachment.setUploadUserId(userId);
        attachment.setAttachmentType("REPLY");
        attachment.setCreateTime(LocalDateTime.now());
        feedbackAttachmentMapper.insert(attachment);

        return attachment;
    }

    public void download(Long attachmentId, HttpServletResponse response) {
        FeedbackAttachment attachment = feedbackAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }

        Path filePath = Paths.get(attachment.getFilePath());
        File file = filePath.toFile();
        if (!file.exists()) {
            throw new BusinessException("附件文件不存在，可能已被删除");
        }

        response.setContentType("application/octet-stream");
        response.setContentLengthLong(attachment.getFileSize());

        String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }
}
