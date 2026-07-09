package com.xju.enrollment.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.Activity;
import com.xju.enrollment.entity.Enrollment;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.mapper.ActivityMapper;
import com.xju.enrollment.entity.Notification;
import com.xju.enrollment.mapper.EnrollmentMapper;
import com.xju.enrollment.mapper.NotificationMapper;
import com.xju.enrollment.mapper.UserMapper;
import com.xju.enrollment.modules.system.service.UserService;
import com.xju.enrollment.modules.workflow.dto.ApprovalRequest;
import com.xju.enrollment.modules.workflow.dto.ApprovalVO;
import com.xju.enrollment.modules.workflow.dto.BatchApprovalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final EnrollmentMapper enrollmentMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final UserService userService;
    private final NotificationMapper notificationMapper;

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVING = "APPROVING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";

    // ---- query pending lists ----

    public PageResult<ApprovalVO> getPendingListForCollege(Long collegeId, Long activityId, int page, int size) {
        Page<Enrollment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getCollegeId, collegeId)
                .eq(Enrollment::getStatus, STATUS_SUBMITTED)
                .eq(activityId != null, Enrollment::getActivityId, activityId)
                .orderByDesc(Enrollment::getCreateTime);

        Page<Enrollment> result = enrollmentMapper.selectPage(pageParam, wrapper);
        List<ApprovalVO> voList = enrichApprovalVOList(result.getRecords());
        return PageResult.of(voList, result.getTotal(), page, size);
    }

    public PageResult<ApprovalVO> getPendingListForSchool(Long collegeId, Long activityId, int page, int size) {
        Page<Enrollment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<Enrollment>()
                .eq(collegeId != null, Enrollment::getCollegeId, collegeId)
                .eq(Enrollment::getStatus, STATUS_APPROVING)
                .eq(activityId != null, Enrollment::getActivityId, activityId)
                .orderByDesc(Enrollment::getCreateTime);

        Page<Enrollment> result = enrollmentMapper.selectPage(pageParam, wrapper);
        List<ApprovalVO> voList = enrichApprovalVOList(result.getRecords());
        return PageResult.of(voList, result.getTotal(), page, size);
    }

    // ---- approve / reject ----

    @Transactional
    public void approve(Long enrollmentId, ApprovalRequest req, Long approverId) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!ACTION_APPROVE.equalsIgnoreCase(req.action())) {
            throw new BusinessException("无效的操作类型");
        }

        User approver = userService.getById(approverId);

        String approverRole = approver.getRole();
        String notificationAction;
        if ("COLLEGE_ADMIN".equals(approverRole)) {
            // College admin approves: SUBMITTED -> APPROVING
            if (!STATUS_SUBMITTED.equals(enrollment.getStatus())) {
                throw new BusinessException("当前状态不允许学院审批");
            }
            enrollment.setStatus(STATUS_APPROVING);
            enrollment.setCurrentNode("school_review");
            enrollmentMapper.updateById(enrollment);
            notificationAction = "COLLEGE_APPROVED";

        } else if ("SCHOOL_ADMIN".equals(approverRole)) {
            // School admin approves: APPROVING -> APPROVED
            if (!STATUS_APPROVING.equals(enrollment.getStatus())) {
                throw new BusinessException("当前状态不允许学校审批");
            }
            enrollment.setStatus(STATUS_APPROVED);
            enrollment.setApprovedAt(LocalDateTime.now());
            enrollment.setGroupName(enrollment.getTargetSchool());
            enrollmentMapper.updateById(enrollment);

            // Re-rank all approved enrollments in the same target-school group
            reRankByTargetSchool(enrollment.getTargetSchool());
            notificationAction = "APPROVED";

        } else {
            throw new BusinessException("无审批权限");
        }

        // Notify applicant
        notifyApplicant(enrollment.getUserId(), enrollment.getActivityId(), notificationAction,
                req.comment());
    }

    @Transactional
    public void batchApprove(BatchApprovalRequest req, Long approverId) {
        if (req.enrollmentIds() == null || req.enrollmentIds().isEmpty()) {
            throw new BusinessException("请选择要审批的记录");
        }
        for (Long enrollmentId : req.enrollmentIds()) {
            ApprovalRequest singleReq = new ApprovalRequest(
                    enrollmentId, req.action(), req.comment(), null);
            approve(enrollmentId, singleReq, approverId);
        }
    }

    @Transactional
    public void reject(Long enrollmentId, ApprovalRequest req) {
        if (!ACTION_REJECT.equalsIgnoreCase(req.action())) {
            throw new BusinessException("无效的操作类型");
        }

        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (STATUS_APPROVED.equals(enrollment.getStatus())
                || STATUS_REJECTED.equals(enrollment.getStatus())) {
            throw new BusinessException("当前状态不允许驳回");
        }

        enrollment.setStatus(STATUS_REJECTED);
        enrollment.setRejectReason(req.rejectReason());
        enrollmentMapper.updateById(enrollment);

        // Notify applicant
        notifyApplicant(enrollment.getUserId(), enrollment.getActivityId(), "REJECTED",
                req.rejectReason());
    }

    // ---- helpers ----

    private List<ApprovalVO> enrichApprovalVOList(List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = enrollments.stream()
                .map(Enrollment::getUserId).distinct().toList();
        List<Long> activityIds = enrollments.stream()
                .map(Enrollment::getActivityId).distinct().toList();

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Activity> activityMap = activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, a -> a, (a, b) -> a));

        return enrollments.stream()
                .map(e -> {
                    User applicant = userMap.get(e.getUserId());
                    Activity activity = activityMap.get(e.getActivityId());
                    return new ApprovalVO(
                            e.getId(),
                            applicant != null ? applicant.getName() : null,
                            applicant != null ? applicant.getRole() : null,
                            activity != null ? activity.getTitle() : null,
                            e.getTargetSchool(),
                            applicant != null ? applicant.getGpa() : null,
                            e.getCollegeName(),
                            e.getStatus(),
                            // collegeApproved: true if status is at or past school review
                            STATUS_APPROVING.equals(e.getStatus())
                                    || STATUS_APPROVED.equals(e.getStatus()),
                            null,
                            e.getCreateTime()
                    );
                })
                .toList();
    }

    /**
     * Re-rank all APPROVED enrollments for the given target-school group.
     * Rank is based on GPA descending: highest GPA = rank 1.
     */
    private void reRankByTargetSchool(String targetSchool) {
        // Query all APPROVED enrollments in this target school
        List<Enrollment> sameGroup = enrollmentMapper.selectList(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getTargetSchool, targetSchool)
                        .eq(Enrollment::getStatus, STATUS_APPROVED)
        );

        if (sameGroup.isEmpty()) {
            return;
        }

        // Collect user GPA info for ranking
        List<Long> userIds = sameGroup.stream()
                .map(Enrollment::getUserId).distinct().toList();
        Map<Long, Double> gpaMap = userMapper.selectBatchIds(userIds).stream()
                .filter(u -> u.getGpa() != null)
                .collect(Collectors.toMap(User::getId, User::getGpa, (a, b) -> a));

        // Sort by GPA ascending (lowest first), then assign descending ranks
        List<Enrollment> ranked = sameGroup.stream()
                .sorted(Comparator.comparingDouble(
                        e -> gpaMap.getOrDefault(e.getUserId(), 0.0)))
                .toList();

        for (int i = 0; i < ranked.size(); i++) {
            Enrollment e = ranked.get(i);
            // rank 1 = highest GPA (last element after ascending sort)
            e.setRankInGroup(ranked.size() - i);
            enrollmentMapper.updateById(e);
        }
    }

    private void notifyApplicant(Long userId, Long activityId, String action, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("APPROVAL");
        notification.setRelatedId(activityId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());

        if ("COLLEGE_APPROVED".equals(action)) {
            notification.setTitle("学院审批通过");
            notification.setContent("您的报名申请已通过学院审批，等待学校终审"
                    + (message != null ? "：" + message : ""));
        } else if ("APPROVED".equals(action)) {
            notification.setTitle("报名审批通过");
            notification.setContent("您的报名申请已通过审批" + (message != null ? "：" + message : ""));
        } else if ("REJECTED".equals(action)) {
            notification.setTitle("报名审批驳回");
            notification.setContent("您的报名申请已被驳回" + (message != null ? "，原因：" + message : ""));
        }

        notificationMapper.insert(notification);
        log.info("Notification created for userId={}, activityId={}, action={}", userId, activityId, action);
    }
}
