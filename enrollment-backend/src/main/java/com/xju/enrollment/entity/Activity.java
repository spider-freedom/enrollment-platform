package com.xju.enrollment.entity;


import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /**
     * ONLINE / OFFLINE
     */
    private String type;

    /**
     * DRAFT / PUBLISHED / ONGOING / ENDED
     */
    private String status;

    /**
     * 1 仅学生 / 2 仅教师 / 3 学生+教师
     */
    private Integer targetAudience;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime enrollStart;

    private LocalDateTime enrollEnd;

    private String location;

    private Integer maxStudents;

    private Integer maxTeachers;

    private Integer maxPerSchool;

    private String bannerUrl;

    private String bannerLink;

    private Integer isBanner;

    private String groupRule;

    private String rankRule;

    private String workflowKey;

    private Long creatorId;


    private LocalDateTime createTime;


    private LocalDateTime updateTime;
}
