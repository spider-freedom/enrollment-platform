package com.xju.enrollment.entity;


import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long userId;

    private String userRole;

    private String content;

    /**
     * Rating 1-5
     */
    private Integer rating;

    private String contact;

    private String department;

    /**
     * SUBMITTED / REPLIED / CLOSED
     */
    private String status;

    private String reply;

    private Long replyUserId;

    private LocalDateTime replyTime;


    private LocalDateTime createTime;


    private LocalDateTime updateTime;
}
