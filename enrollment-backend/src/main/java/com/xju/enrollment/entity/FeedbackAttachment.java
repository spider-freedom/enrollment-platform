package com.xju.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback_attachment")
public class FeedbackAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long feedbackId;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Long uploadUserId;

    /**
     * REPLY
     */
    private String attachmentType;

    private LocalDateTime createTime;
}
