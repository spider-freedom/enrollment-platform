package com.xju.enrollment.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
     * 宣讲会/开放日/夏令营/咨询会/回访母校/线上直播/其他
     */
    private String category;

    /**
     * 院级 / 校级
     */
    private String level;

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

    private Long collegeId;

    private String collegeName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
