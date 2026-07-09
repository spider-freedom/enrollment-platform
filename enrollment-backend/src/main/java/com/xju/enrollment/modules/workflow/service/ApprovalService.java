package com.xju.enrollment.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.Activity;
import com.xju.enrollment.entity.Enrollment;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.mapper.ActivityMapper;
import com.xju.enrollment.mapper.EnrollmentMapper;
import com.xju.enrollment.mapper.UserMapper;
import com.xju.enrollment.modules.system.service.UserService;
import com.xju.enrollment.modules.workflow.dto.ApprovalRequest;
import com.xju.enrollment.modules.workflow.dto.ApprovalVO;
import com.xju.enrollment.modules.workflow.dto.BatchApprovalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final EnrollmentMapper enrollmentMapper;
    private final ActivityMapper activityMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    public PageResult<ApprovalVO> getPendingListForCollege(Long collegeId, Long activityId, int page, int size) {
        LambdaQueryWrapper<Enrollment> w = new LambdaQueryWrapper<>();
        w.eq(Enrollment::getCollegeId, collegeId);
        w.eq(Enrollment::getStatus, "SUBMITTED");
        if (activityId != null) w.eq(Enrollment::getActivityId, activityId);
        w.orderByDesc(Enrollment::getCreateTime);
        Page<Enrollment> p = enrollmentMapper.selectPage(new Page<>(page, size), w);
        return PageResult.of(p.getRecords().stream().map(this::toVO).toList(), p.getTotal(), page, size);
    }

    public PageResult<ApprovalVO> getPendingListForSchool(Long collegeId, Long activityId, int page, int size) {
        LambdaQueryWrapper<Enrollment> w = new LambdaQueryWrapper<>();
        w.eq(Enrollment::getStatus, "APPROVING");
        if (activityId != null) w.eq(Enrollment::getActivityId, activityId);
        if (collegeId != null) w.eq(Enrollment::getCollegeId, collegeId);
        w.orderByDesc(Enrollment::getCreateTime);
        Page<Enrollment> p = enrollmentMapper.selectPage(new Page<>(page, size), w);
        return PageResult.of(p.getRecords().stream().map(this::toVO).toList(), p.getTotal(), page, size);
    }

    @Transactional
    public void approve(Long enrollmentId, ApprovalRequest req, Long approverId) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) throw new BusinessException("报名记录不存在");
        User approver = userService.getById(approverId);
        String role = approver.getRole();

        if ("COLLEGE_ADMIN".equals(role)) {
            if (!"SUBMITTED".equals(enrollment.getStatus()))
                throw new BusinessException("当前状态不可审批");
            enrollment.setStatus("APPROVING");
        } else if ("SCHOOL_ADMIN".equals(role)) {
            if (!"APPROVING".equals(enrollment.getStatus()))
                throw new BusinessException("当前状态不可审批");
            enrollment.setStatus("APPROVED");
            enrollment.setApprovedAt(LocalDateTime.now());
            // Auto group by target school, rank by GPA
            assignGroupAndRank(enrollment);
        } else {
            throw new BusinessException("无审批权限");
        }
        enrollmentMapper.updateById(enrollment);
    }

    @Transactional
    public void reject(Long enrollmentId, ApprovalRequest req) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) throw new BusinessException("报名记录不存在");
        enrollment.setStatus("REJECTED");
        enrollment.setRejectReason(req.rejectReason());
        enrollmentMapper.updateById(enrollment);
    }

    @Transactional
    public void batchApprove(BatchApprovalRequest req, Long approverId) {
        for (Long id : req.enrollmentIds()) {
            approve(id, new ApprovalRequest(id, req.action(), req.comment(), null), approverId);
        }
    }

    private void assignGroupAndRank(Enrollment enrollment) {
        String school = enrollment.getTargetSchool();
        if (school == null || school.isEmpty()) return;
        enrollment.setGroupName(school + "组");
        List<Enrollment> group = enrollmentMapper.selectList(
            new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getTargetSchool, school)
                .eq(Enrollment::getStatus, "APPROVED")
        );
        List<Enrollment> sorted = new ArrayList<>(group);
        sorted.add(enrollment);
        sorted.sort(Comparator.comparing(e -> {
            User u = userMapper.selectById(e.getUserId());
            return u != null && u.getGpa() != null ? -u.getGpa() : 0;
        }));
        for (int i = 0; i < sorted.size(); i++) {
            Enrollment e = sorted.get(i);
            e.setRankInGroup(i + 1);
            enrollmentMapper.updateById(e);
        }
    }

    private ApprovalVO toVO(Enrollment e) {
        User u = userMapper.selectById(e.getUserId());
        Activity a = activityMapper.selectById(e.getActivityId());
        return new ApprovalVO(
            e.getId(), u != null ? u.getName() : "未知", u != null ? u.getRole() : "",
            a != null ? a.getTitle() : "", e.getTargetSchool(),
            u != null && u.getGpa() != null ? u.getGpa() : 0.0,
            e.getCollegeName(), e.getStatus(), "APPROVING".equals(e.getStatus()),
            e.getRejectReason(), e.getCreateTime()
        );
    }
}
