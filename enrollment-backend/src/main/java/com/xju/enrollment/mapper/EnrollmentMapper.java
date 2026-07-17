package com.xju.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xju.enrollment.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    /**
     * 按活动+报名人角色统计有效报名人数（不含已撤回/已拒绝）
     * 返回字段: activityId, role, cnt
     */
    @Select("<script>" +
            "SELECT e.activity_id AS activityId, u.role AS role, COUNT(*) AS cnt " +
            "FROM enrollment e JOIN sys_user u ON e.user_id = u.id " +
            "WHERE e.status NOT IN ('WITHDRAWN', 'REJECTED') " +
            "AND e.activity_id IN " +
            "<foreach item='id' collection='activityIds' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY e.activity_id, u.role" +
            "</script>")
    List<Map<String, Object>> countByActivityAndRole(@Param("activityIds") List<Long> activityIds);
}