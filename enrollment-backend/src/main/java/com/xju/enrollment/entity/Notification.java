package com.xju.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    /**
     * APPROVAL / FEEDBACK / SYSTEM
     */
    private String type;

    /**
     * 0 未读 / 1 已读
     */
    private Integer isRead;

    private Long relatedId;

    private LocalDateTime createTime;
}
