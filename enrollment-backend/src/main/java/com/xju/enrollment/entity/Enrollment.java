package com.xju.enrollment.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enrollment")
public class Enrollment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long userId;

    /**
     * SUBMITTED / APPROVING / APPROVED / REJECTED / WITHDRAWN
     */
    private String status;

    /**
     * JSON string containing form field values
     */
    private String formData;

    private String currentNode;

    private Long collegeId;

    private String collegeName;

    private String targetSchool;

    private Integer rankInGroup;

    private String groupName;

    private String rejectReason;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;


    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
