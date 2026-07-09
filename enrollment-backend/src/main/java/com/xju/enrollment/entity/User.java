package com.xju.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private Long collegeId;
    private String collegeName;
    private String major;
    private String grade;
    private Double gpa;
    private String email;
    private String phone;
    private String avatar;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
