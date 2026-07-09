package com.xju.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("activity_field_config")
public class ActivityFieldConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private String fieldName;

    private String fieldLabel;

    private String fieldType;

    /**
     * 0 非必填 / 1 必填
     */
    private Integer required;

    private Integer sortOrder;

    /**
     * JSON string for select/radio/checkbox options
     */
    private String options;

    /**
     * 0 禁用 / 1 启用
     */
    private Integer enableAiNormalize;
}
